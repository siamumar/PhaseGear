#include <EEPROM.h>

// I2Cdev and MPU6050 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "MPU6050.h"

// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
    #include "Wire.h"
#endif

//#define OUTPUT_READABLE_ACCELGYRO
//#define OUTPUT_BINARY_ACCELGYRO

#define HAP_PIN 9
#define LED_PIN2 13
#define TH_FAST_DEFAULT 12000
#define TH_AX_DEFAULT 15000
#define TH_GY_DEFAULT 10000
#define TH_FAST_ADDR 0

// class default I2C address is 0x68
// specific I2C addresses may be passed as a parameter here
// AD0 low = 0x68 (default for InvenSense evaluation board)
// AD0 high = 0x69
MPU6050 accelgyro;
//MPU6050 accelgyro(0x69); // <-- use for AD0 high

int16_t ax, ay, az;
int16_t gx, gy, gz;

int16_t  AX[7] = {0};
int16_t  AY[7] = {0};
int16_t  AZ[7] = {0};
int16_t  GX[7] = {0};
int16_t  GY[7] = {0};
int16_t  GZ[7] = {0};

char state = '0';

int reps = 0, w_reps = 0;
int16_t dif = 0, max_dif = 0;
double mean_dif;
int16_t th_fast, th_gy = TH_GY_DEFAULT, th_ax = TH_AX_DEFAULT;
int8_t th_fast_0, th_fast_1;
bool gy_peak = false, fast = false, really_fast = false;
int vib_amplitude = 0, vib_duration = 0;

char bt_msg[12];

bool blinkState = false;



void setup()  {  
 // join I2C bus (I2Cdev library doesn't do this automatically)
    #if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
        Wire.begin();
    #elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
        Fastwire::setup(400, true);
    #endif

    // initialize serial communication
    Serial.begin(115200);

    // initialize device
    Serial.println("Initializing I2C devices...");
    accelgyro.initialize();

    // verify connection
    Serial.println("Testing device connections...");
    Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");
   
    pinMode(LED_PIN2, OUTPUT); 
} 

void loop()  { 
  
  delay(100);

  if(Serial.available() > 0){  
      state = Serial.read();
    
       if (state == '1'){
         th_fast_0 = EEPROM.read(TH_FAST_ADDR);
         th_fast_1 = EEPROM.read(TH_FAST_ADDR+1);         
         th_fast = th_fast_1*255+th_fast_0;
         sprintf(bt_msg, "t-%d,%d,%d", th_fast,th_fast_1,th_fast_0);
         Serial.println(bt_msg);
       }      
       
       if ((state == '1')||(state == '2')) {
          reps = 0;
          w_reps = 0;
          mean_dif = 0;
       }
       
       if (state == '3'){
          th_fast_0 = TH_FAST_DEFAULT%255;
          th_fast_1 = floor(TH_FAST_DEFAULT/255);
          EEPROM.write(TH_FAST_ADDR, th_fast_0);
          EEPROM.write(TH_FAST_ADDR+1, th_fast_1);
          th_fast_0 = 0;
          th_fast_1 = 0;
          state = '0';
       } 
   }
   
   if(state == '0') return;

   // read raw accel/gyro measurements from device    
     accelgyro.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
     
     for (int k = 1; k <= 6; k++){
       AX[k-1] = AX[k];
       AY[k-1] = AY[k];
       AZ[k-1] = AZ[k];
       GX[k-1] = GX[k];
       GY[k-1] = GY[k];
       GZ[k-1] = GZ[k];
     }
     
    AX[6] = ax;
    AY[6] = ay;
    AZ[6] = az;
    GX[6] = gx;
    GY[6] = gy;
    GZ[6] = gz; 
    
    #ifdef OUTPUT_READABLE_ACCELGYRO
    Serial.println(ax); //Serial.print("\t");
    Serial.println(ay); //Serial.print("\t");
    Serial.println(az); //Serial.print("\t");
    Serial.println(gx); //Serial.print("\t");
    Serial.println(gy); //Serial.print("\t");
    Serial.println(gz);    
    #endif
    
    #ifdef OUTPUT_BINARY_ACCELGYRO
    Serial.write((uint8_t)(ax >> 8)); Serial.write((uint8_t)(ax & 0xFF));
    Serial.write((uint8_t)(ay >> 8)); Serial.write((uint8_t)(ay & 0xFF));
    Serial.write((uint8_t)(az >> 8)); Serial.write((uint8_t)(az & 0xFF));
    Serial.write((uint8_t)(gx >> 8)); Serial.write((uint8_t)(gx & 0xFF));
    Serial.write((uint8_t)(gy >> 8)); Serial.write((uint8_t)(gy & 0xFF));
    Serial.write((uint8_t)(gz >> 8)); Serial.write((uint8_t)(gz & 0xFF));
    #endif
    
        // Get difference
    if(GY[6] > GY[5]) dif = GY[6]-GY[5];
    else dif = GY[5]-GY[6];
    
    if(state == '1'){
      if(dif > th_fast) fast = 1; 
    }
    else if(state == '2'){
      if (dif > max_dif) max_dif = dif;
      fast = 0; // so that really_fast is always 0 and we count all the reps, there is no wrong rep in training phase
    }
     
    // count Reps  
    if ( (GY[3] > th_gy) && (GY[0] < /*GY[1]) && (GY[1] < GY[2]) && (GY[2] <*/ GY[3]) && (GY[3] > /*GY[4]) && (GY[4] > GY[5]) && (GY[5] >*/ GY[6]) ) 
    {
          gy_peak = 1;
    }
    
    if ( gy_peak && (AX[0] < /*AX[1]) && (AX[1] < AX[2]) && (AX[2] <*/ AX[3]) && (AX[3] > /*AX[4]) && (AX[4] > AX[5]) && (AX[5] >*/ AX[6]) ) 
    {
          really_fast = (fast && (AX[3] < th_ax));
          if (!really_fast) {
            reps = reps+1;
          }
          else {
            w_reps = w_reps+1;
          }
          sprintf(bt_msg, "%d-%d-%d-%d", reps, w_reps, fast, really_fast);
          Serial.println(bt_msg);
          fast = 0;
          gy_peak = 0;
          
          if(state == '2'){
            mean_dif = mean_dif+max_dif/10;
            max_dif = 0;
            if (reps == 10) {
              th_fast = round(mean_dif);
              th_fast_0 = th_fast%255;
              th_fast_1 = floor(th_fast/255);
              
              EEPROM.write(TH_FAST_ADDR, th_fast_0);
              EEPROM.write(TH_FAST_ADDR+1, th_fast_1);
              
              delay(100);
              sprintf(bt_msg, "t-%d,%d,%d", th_fast,th_fast_1,th_fast_0);
              Serial.println(bt_msg);
              
              state = '0';
            }
          }
    }
    
      //----------------------------------------------------------------------------- 
   if(really_fast) {
     vib_duration = 5;
     vib_amplitude = 255;
     really_fast = 0;
   }
 
   if(vib_duration) vib_duration--;
   else vib_amplitude = 0;
 
   analogWrite(HAP_PIN, vib_amplitude);   
  
  // blink LED to indicate activity
    blinkState = !blinkState;
    digitalWrite(LED_PIN2, blinkState); 

}
