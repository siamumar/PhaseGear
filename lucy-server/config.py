# LUCY-SERVER CONFIGURATION FILE

# Configuration file for the core functionality of the lucy-server Flask web app for interfacing between the Lucy
# and Mood Reminder Android applications with the remote server.
# See the comments for each constant for configuration instructions.
# All fields are mandatory for the functionality of the application unless otherwise indicated.

# Kevin Lin, Rice University, 1/3/2015


# CAS AUTHENTICATION
# String representation of the fully qualified URL of the CAS server to be used for authentication.
# To disable the CAS authentication functionality, initialize this variable to None.
CAS_SERVER_URL = "https://netid.rice.edu"

# List of string representations of Net IDs that are permitted access to the web administration interface.
ALLOWED_CAS_USERS = ["kl38", "pyw1"]


# MYSQL DATABASE
# Name of the database. This must match the name of the database created on the local MySQL server.
DATABASE_NAME = "EmpData"

# Username to access the database
DATABASE_LOGIN_USERNAME = "root"

# Password to access the database
DATABASE_LOGIN_PASSWORD = "56289086"
