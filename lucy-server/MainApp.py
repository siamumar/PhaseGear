from flask import Flask, request, g, make_response, send_from_directory, render_template, redirect, session
from flaskext.mysql import MySQL
from flask.ext.cas import CAS
import json
import config


mysql = MySQL()
app = Flask(__name__, static_folder='static', static_url_path='/static')
cas = CAS(app)
app.config['CAS_SERVER'] = config.CAS_SERVER_URL
app.config['CAS_AFTER_LOGIN'] = '/after_login'

app.config['MYSQL_DATABASE_USER'] = config.DATABASE_LOGIN_USERNAME
app.config['MYSQL_DATABASE_PASSWORD'] = config.DATABASE_LOGIN_PASSWORD
app.config['MYSQL_DATABASE_DB'] = config.DATABASE_NAME
app.config['MYSQL_DATABASE_HOST'] = 'localhost'

mysql.init_app(app)


@app.before_request
def before_request():
    g.db = mysql.get_db()
    g.cursor = mysql.get_db().cursor()


@app.route("/", methods=["GET", "POST"])
def route_root():
    if config.CAS_SERVER_URL:
        # Get the logged in Net ID, and redirect the user to /login is no one is logged in
        net_id = session.get(app.config['CAS_USERNAME_SESSION_KEY'], None)
        if not net_id:
            return redirect("/login")

    if config.CAS_SERVER_URL is None or net_id in config.ALLOWED_CAS_USERS:
        if request.method == 'GET':
            return render_template("index.html")
        else:
            table = request.form.get("table")
            uuid = request.form.get("uuid")
            if uuid:
                return redirect("/admin?table=%s&uuid=%s" % (table, uuid))
            else:
                return redirect("/admin?table=%s" % table)
    else:
        return net_id + " is not an authorized user per this application's configuration."


@app.route('/after_login', methods=['GET'])
def after_login():
    return redirect("/")


@app.route('/admin')
def print_all():
    # tables = ["accelerometer", "app", "calls", "cellular", "device", "loc", "mood", "network", "screen", "sms", "steps", "web", "wifi"]
    # return table
    uuid = request.args.get("uuid", "")
    table = request.args.get("table", "")
    try:
        if uuid:
            query = "SELECT * FROM %s WHERE uuid=%s" % (table, uuid)
        else:
            query = "SELECT * FROM %s" % table
        g.cursor.execute(query)
        entries = g.cursor.fetchall()
        g.cursor.execute("SELECT column_name FROM information_schema.columns WHERE table_name='%s'" % table)
        names = g.cursor.fetchall()
    except:
        return "exception"

    return render_template("admin.html", entries=entries, table=table, names=names)


@app.route('/admin.php')
def static_from_root():
    return send_from_directory(app.static_folder, request.path[1:])


# use get for testing puspose, change to post later
@app.route("/upload", methods=["POST"])
def receiver():
    raw_json_str = request.form["json"]
    # return raw_json_str
    uuid = request.form["uuid"]
    data_type = request.form["data_type"]
    """
    #for testing purpose
    raw_json_str = request.args.get("json","")
    #return raw_json_str
    uuid = request.args.get("uuid","")
    data_type = request.args.get("data_type","")
    #return data_type
    """
    g.cursor.execute("SHOW TABLES LIKE \'%s\'" % data_type)
    table = g.cursor.fetchone()
    # return table
    response_text = ""

    # if there is no table, don't do anything to the database
    # rather than create a corresponding table for security reason
    if table is None:
        resp = make_response("there is no such table")
        resp.headers["Content-Type"] = "text/plain"
        resp.headers["charset"] = "UTF-8"
        return resp
    else:
        json_array = parseJson(raw_json_str)
        # if json_array == None:
        # return "cannot parse json"
        if not json_array:
            # return "data cannot be parsed as json: %s" % raw_json_str
            response_text = "data of %s cannot be parsed as json: %s" % (table, raw_json_str)
            resp = make_response(response_text)
            resp.headers["Content-Type"] = "text/plain"
            resp.headers["charset"] = "UTF-8"
            return resp
        # for each row, we shoud have: uuid, data1, data2...
        # since we don't know how many columns each data type may need, use another
        # helper method to convert a json_object to list of rows
        #and insert them
        for json_obj in json_array:
            #construct query, it must start with uuid
            columns = "uuid,"
            values = "'" + uuid + "'" + ","
            for item in json_obj.items():
                columns += item[0] + ","
                values += "'" + item[1] + "'" + ","
            #correct the last comma
            columns = columns[:-1]
            values = values[:-1]
            try:
                query = "INSERT INTO %s (%s) VALUES (%s)" % (table[0], columns, values)
                g.cursor.execute(query)
                g.db.commit()
                response_text += "successfully write into table: %s \n" % table
            except:
                g.db.rollback()
                #return "fail to write into table: %s, rollback \n query: %s" % (table, query)
                response_text += "fail to write into table: %s \n query: %s" % (table, query)

    resp = make_response(response_text)
    resp.headers["Content-Type"] = "text/plain"
    resp.headers["charset"] = "UTF-8"
    return resp


def parseJson(json_str):
    try:
        json_obj = json.loads(json_str)
        return json_obj
    except:
        return None


if __name__ == "__main__":
    # app.debug = True
    # app.run(host='0.0.0.0')
    app.run(debug=True)
