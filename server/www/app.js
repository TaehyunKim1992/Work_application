var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var routes = require('./routes/index');
var users = require('./routes/users');
var usermonitering = require('./routes/usermonitering');
var testpage = require('./routes/testpage');

var app = express();
var http = require('http').Server(app); // 추가
var io = require('socket.io')(http); // 추가
var mariadb = require('mysql');

var usercount = 0;
var userlist = new Array();

process.on('uncaughtException', function (err) {
	//예상치 못한 예외 처리
	console.log('uncaughtException 발생 : ' + err);

  console.log("\u001b[0m");
});

var conn = mariadb.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : '0000',
  database : 'monitering'
});



// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.set('port', 3000 );

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/usermonitering', usermonitering);
app.use('/', routes);
app.use('/users', users);
app.use('/testpage', testpage);


http.listen(app.get('port'), function(){
    console.log('Express server listening on port ' + app.get('port'));
    //console.log(new Buffer("Hello World").toString('base64'));
}); // 추가

var testInterval = setInterval(function(){
    //var sql = 'SELECT email, TIMEDIFF(CURRENT_TIMESTAMP,time)+0 > 3000 AS time FROM lastupdate'
    var sql = 'SELECT email, (TIMEDIFF(CURRENT_TIMESTAMP,time)+0 > 3000) AS time FROM lastupdate WHERE (TIMEDIFF(CURRENT_TIMESTAMP,time)+0 > 3000) = 1';
    conn.query(sql, function(err, rows, fields){
      if(err) throw err;
        for(var i=0; i<rows.length; i++){
        console.log(rows[i].email+"   state: "+rows[i].time);
          if(rows[i].time = 1){
            // var sql2 = 'UPDATE user SET devicenum=9999 WHERE email=\''+rows[i].email+'\''
            // conn.query(sql2, function(err, rows, fields){
            //   if(err) throw err;
            // });
            var sql2 = 'UPDATE user SET devicenum=0 WHERE email=?'
            //해당부분 트리거로 변경하기
            conn.query(sql2, [rows[i].email], function(err, rows, fields){
              if(err) throw err;
            });
          }
      }
    });
},600000);


/*** Socket.IO 추가 ***/
io.on('connection', function(socket){
    usercount++;
    //console.log('a user connected');

    io.emit('userin',usercount);

    socket.broadcast.emit('hi');

    socket.on('disconnect', function(){
        usercount--;
        //console.log('user disconnected' + socket.id);

        io.emit('userout',usercount);
    });


    //모바일 처리부분 - 삭제예정
    socket.on('message',function(data){
        socket.broadcast('message',data);
        //console.log(data);
        if(data.name && data.text && data.name.indexOf('\\') === -1 && data.text.indexOf('\\') === -1){
          var sql = 'INSERT INTO chatlist(name,content) VALUES(\''+data.name+'\',\''+data.text+'\')';
          conn.query(sql, function(err, rows, fields){
            if(err) throw err;
            console.log('Ok. Query!');
          });
        }
    })

    // 삭제예정
    socket.on('useralert',function(data){
        var findflag = 1;
        var sql = 'INSERT INTO userlist(name,email,socketid,state) VALUES(\''+data.username+'\',\''+data.email+'\',\''+socket.id+'\',\''+data.checklo+'\')'
        var sql2 = 'UPDATE userlist SET state=(\''+data.checklo+'\'),socketid=(\''+socket.id+'\') WHERE email=\''+data.email+'\''

        io.emit('useralert',data);
        io.emit('count',usercount);

        for(i=0;i<userlist.length;i++){
          if(userlist[i].name.indexOf(data.username) === 0 &&
             userlist[i].email.indexOf(data.email) === 0 || userlist[i].name.indexOf(data.username) === 0 ){
              findflag = 0;
              conn.query(sql2, function(err, rows, fields){
                if(err) throw err;
                console.log('user state update');
              });
          }
        }
        if(findflag === 1){
          userlist.push({name:data.username,email:data.email,socketid:socket.id,state:data.checklo});
          conn.query(sql, function(err, rows, fields){
            if(err) throw err;
            console.log('New User come!!');
          });
        }
    });

    //사용자 비콘정보 업데이트
    socket.on('userlocation',function(data){
        var findbeacon = 'SELECT * FROM device WHERE uuid=(\''+data.uuid+'\') AND major=(\''+data.major+'\') AND minor=(\''+data.minor+'\')'
        console.log(findbeacon + "    " + data.email);

        conn.query(findbeacon,function(err,rows,fields){
          if(err) {
            console.log(findbeacon);
            console.log('해당 비콘을 DB에서 찾을 수 없습니다!');
            throw err;
          }
          // var insertsql = 'INSERT INTO user(name,email,devicenum) VALUES(\''+data.username+'\',\''+data.email+'\','+rows[0].num+')'
          //console.log(rows[0]);
          try{
          var updateloc = 'UPDATE user SET devicenum=(\''+rows[0].num+'\') WHERE email=\''+data.email+'\''
          }
          catch(exception){
            console.log('Error! 해당 비콘에 대한 정보는 데이터베이스에 없습니다.')
          }
          //console.log(updateloc);
          var upatetime = 'UPDATE lastupdate SET time=(CURRENT_TIMESTAMP) WHERE email=\''+data.email+'\''
          conn.query(updateloc,function(err,rows,fields){
              if(err) {
                console.log('해당 비콘을 DB에서 찾을 수 없습니다!');
                throw err;
              }
          });
          conn.query(upatetime,function(err,rows,fields){
              if(err) throw err;
          });
          // socket.emit('loc',rows[0].loc);
        });
    });

    //사용자 위치정보 뷰
    //socket.on('viewuser',function(data){
    socket.on('userlocation',function(data){
      var viewloc = 'SELECT name,loc,type FROM user JOIN device ON(device.num = user.devicenum) WHERE email = \''+data.email+'\'';
      console.log(viewloc);
      conn.query(viewloc,function(err,rows,fields){
          if(err) throw err;
          if((rows[0].name).length>2){
          console.log('\u001b[32m',rows[0].name+" : "+rows[0].loc);
          console.log("\u001b[0m");

          }
          // socket.emit('mylocation',rows[0].loc);
          socket.emit('mylocation',rows[0]);
      });
    })


    //로그인처리 (1)
    socket.on('finduser',function(data){
      var getemail = 'SELECT email FROM user WHERE BINARY(email = \''+data.email+'\' AND password = \''+data.pwd+'\')';
      console.log(getemail);
      conn.query(getemail,function(err,rows,fields){
          if(err) throw err;
          if(rows[0] === undefined){
            socket.emit('loginfalse');
            console.log('회원 정보 없음');
          }
          else{
            socket.emit('loginok');
            console.log(rows[0].email);
          }
      });
    })

    //회원가입처리 (로그인(1)과 동일하여 통합예정) 비밀번호 암호화예정
    socket.on('joinuser',function(data){
      var getemail = 'SELECT email FROM user WHERE email = \''+data.email+'\'';
      var userjoin = 'INSERT INTO user VALUES(\''+data.email+'\',\''+data.pwd+'\',\''+data.name+'\',NULL)'
      var insertlastup = 'INSERT INTO lastupdate VALUES(\''+data.email+'\',CURRENT_TIMESTAMP)'
      conn.query(getemail,function(err,rows,fields){
          if(err) throw err;
          if(rows[0] === undefined){
            conn.query(userjoin,function(err,rows,fields){
              if(err) throw err;
            });
            conn.query(insertlastup,function(err,rows,fields){
              if(err) throw err;
            });
            socket.emit('joinok');
            console.log('가입가능');
          }
          else{
            socket.emit('joinfalse');

           console.log(rows[0].email);
          }
      });
    })

    //회원 탈퇴 기능
    socket.on('memberleave',function(data){
      var deletemember = 'DELETE FROM user WHERE email=? AND password=?';
      conn.query(deletemember,[data.email,data.pwd],function(err,rows,fields){
        if(err) {
          throw err;
        }
        else{
          console.log('탈퇴요청'+data.email);
          socket.emit('memberleaveok');
          console.log('탈퇴완료');
        }
      });
    });


    //socket.io 1:1 알림 제공
    socket.on('alram',function(data){

    })


    //공지사항 제공
    socket.on('showNoticeList',function(data){
      var queryNotice = 'SELECT num,subject FROM notice';
      conn.query(queryNotice,function(err,rows,fields){
          if(err) throw err;
          socket.emit('noticeList',rows);
          socket.emit('viewflag');
          console.log(rows);
      });
    });


    //사용자가 누른 글제목 받아오기
    socket.on('sendsubject',function(data){
      var querySubject = 'SELECT content FROM notice WHERE subject=?';
      conn.query(querySubject,data,function(err,rows,fields){
          if(err) throw err;
          socket.emit('sendcontent',rows[0].content);
          //console.log(rows[0].content);
      });
    });


});

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

// production error handler
// no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
    message: err.message,
    error: {}
  });
});

module.exports = app;
