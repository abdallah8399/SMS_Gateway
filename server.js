var express = require("express");
var url = require("url");
var mysql = require('mysql');

var srv = express();
var mysqlcon = mysql.createConnection({
host: "localhost",
user: "root",
password: "",
database : "sms_gateway_db"} );
var q;
mysqlcon.connect( function(err) {
if (err) throw err;
console.log("Connected!");
});



srv.get("/getSMS", function (req, res) {
    var sql = "SELECT * FROM SMS_Table WHERE `Sent` = 0 ORDER BY `Time` LIMIT 1;"
    mysqlcon.query(sql, function (err, result) {
        if (err) throw err;
        console.log(result);
        res.end(JSON.stringify(result[0]));

        if(result[0]!= null){
            var sql_update = "UPDATE SMS_Table SET Sent = 1 WHERE ID = ?"
            mysqlcon.query(sql_update, result[0].ID , function (err) {
                if (err) throw err;
                console.log("updated");
                });
        }
        });
        });
    
    

srv.get("/sendSMS", function (req, res) {
    q = url.parse(req.url, true).query;
    var sql = "INSERT INTO SMS_Table (Phone, Body) VALUES ("
    sql = sql + q.phone + ", " + q.message + ")";
    console.log(sql);
    mysqlcon.query(sql, function (err, result) {
    if (err) throw err;
    console.log("1 record inserted");
    res.status(200).end();
    });
});
    srv.listen (8080, function () {
    console.log("Server is listening on port 8080."); }
    );