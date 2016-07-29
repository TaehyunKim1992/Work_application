var express = require('express');
var router = express.Router();
var mariadb = require('mysql');

var conn = mariadb.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : '0000',
  //database : 'chatdb'
  database : 'monitering'
});

/* GET home page. */
router.get('/', function(req, res, next) {
  var sql = 'SELECT name,loc,time AS time FROM user JOIN device ON(device.num = user.devicenum) JOIN lastupdate ON(user.email = lastupdate.email)'
  conn.query(sql, function(err, list, fields){
    res.render('testpage', {list:list});
});
});

module.exports = router;
