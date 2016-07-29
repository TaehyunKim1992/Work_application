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
  //var sql = 'SELECT name,loc,time FROM user JOIN device ON(device.num = user.devicenum) JOIN lastupdate ON(user.email = lastupdate.email);'
  var sql = 'SELECT name,loc,time FROM user JOIN device ON(device.num = user.devicenum) JOIN lastupdate ON(user.email = lastupdate.email) WHERE num NOT IN(0);'
  //var sql = 'SELECT name,loc,TO_CHAR(EXTRACT(DAY_HOUR FROM time)) as aaa FROM user JOIN device ON(device.num = user.devicenum) JOIN lastupdate ON(user.email = lastupdate.email) ;'
  conn.query(sql, function(err, list, fields){
    res.render('usermonitering', {list:list});
  })
  //res.render('usermonitering', { title: '모니터링 페이지' });
});

module.exports = router;
