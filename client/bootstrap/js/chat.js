var websocket=null;
var isAuth=false;
var userNick=null;
var userCount=0;

$(document).ready(function(){
    /*bootstrap模态窗插件*/
    $("#menuModal").modal('show');
    var height = $(window).height();
    $('#content').css("height", height - $('#top').height() - $('#opt').height() - 40);

    $('#faceBtn').qqFace({
        id: 'facebox',
        assign: 'mess',
        path: 'arclist/'	//表情存放的路径
    });

    $("#loginBtn").click(function(){
        login();
    })

    $("#sendBtn").click(function () {
        var message=$("#mess").val().trim();
        sendMess(message);
        /*发送消息后清空输入框*/
        $("#mess").val("");
    })

    $("#recoBtn").click(function(){
        send(isAuth,"{'code':10008}");
    })

    $("#deleteBtn").click(function(){
        $("#content").html("");
    })

});

function login(){
    if(!userNick){
        /*获取表单字段的值*/
        userNick=$("#nick").val().trim();
        //alert(userNick);
    }
    if(userNick){
        if(window.WebSocket){
            websocket=new WebSocket("ws://localhost:8888/websocket");
            websocket.onmessage=function (e) {
                var data=eval("("+e.data+")");
                //console.log(data);
                switch (data.uri){
                    //pong消息
                    case 1 << 8 | 220:
                    //ping消息
                    case 2 << 8 | 220:
                        console.log("ping message"+JSON.stringify(data));
                        pingService();
                        break;
                    //系统消息
                    case 3 << 8 | 220:
                        systemService(data);
                        console.log("system message"+JSON.stringify(data));
                        break;
                    case 4 << 8 | 220:
                        console.log("error message"+JSON.stringify(data));
                        break;
                    case 5 << 8 | 220:
                        console.log("auth message"+JSON.stringify(data));
                        break;
                    //群发消息
                    case 6 << 8 | 220:
                        broadcastService(data);
                        /*注意是stringify*/
                        console.log("broadcast message"+JSON.stringify(data));
                        break;
                    //聊天记录
                    case 7 << 8 | 220:
                        console.log("record message"+JSON.stringify(data))
                        recordService(data);
                        break;
                }
            }

            websocket.onerror=function () {
                errorService();
            }

            websocket.onclose=function(){
                closeService();
            }

            websocket.onopen=function () {
                openService();
            }
        }else{
            alert("该浏览器不支持websocket");
        }
    }else{
        $("#tipMsg").text("请输入昵称");
        $("#tipModal").modal('show');
    }

};

function sendMess(message){
    /*里面单引号相当于双引号？*/
    send(isAuth,"{'code':10086,'mess':'"+message+"'}");
};

function openService(){
    console.log("连接成功");
    var obj={};
    obj.code=10000;
    obj.nick=userNick;
    send(true,JSON.stringify(obj));
};

function closeService(){
    console.log("关闭连接");
    websocket=null;
    isAuth=false;
    userCount=0;
    $("#tipMsg").text("网络连接异常");
    $("#tipModal").modal('show');
};

function errorService(){
    console.log("连接存在错误")
}



function broadcastService(data){
    var mess=data.body;
    var nick=data.extend.nick;
    var uid=data.extend.uid;
    var time=data.extend.time;
    var newmess=replace_em(mess);
    var html='<div class="title">'+nick+'&nbsp;('+uid+')&nbsp;'+time+'</div><div class="item">'+newmess+'</div>';
    $("#content").append(html);
    $("#content").scrollTop($("#content")[0].scrollHeight);
};

function pingService(){
    send(isAuth,"{'code':10016}");
};


function systemService(data){
    //console.log(data);
    var code=data.extend.code;
    switch(code){
        /*在线人数*/
        case 20001:
            $("#userCount").text(data.extend.mess);
            break;
        /*认证结果*/
        case 20002:
            isAuth=data.extend.mess;
            if(isAuth){
                $("#menuModal").modal('hide');
                $("#chatWin").show();
                /*$("#content").append("<div class='title'>欢迎"+userNick+"来到聊天室</div>");*/
            }
            break;
        case 20003:
            var html="<div class='greet'>欢迎"+data.extend.mess+"加入聊天室</div>";
            $("#content").append(html);
            break;
        case 20004:
            //console.log("好友列表"+data.extend.mess);
            var list=data.extend.mess;
            $("#userList").html("");
            for(var i=0;i<list.length;i++){
                var temp="<li>"+list[i]+"</li>";
                //console.log("好友列表显示："+temp);
                $("#userList").append(temp);
            }
            break;
        case 20005:
            var html="<div class='exit'>"+data.extend.mess+"离开了聊天室</div>";
            $("#content").append(html);
            break;

    }
};

function recordService(data) {
    var mess=data.body;
    //替换表情之后不能显示为html，未解决
    //var newmess=replace_em(mess);
    $("#RecoMsg").html("聊天记录</br>"+mess);
    $("#RecoModal").modal('show');
}

function send(auth,mess) {
    if(websocket==WebSocket.OPEN||auth){
        console.log("send:"+mess);
        websocket.send(mess);
    }

};

function replace_em(str) {
    str = str.replace(/\</g, '&lt;');
    str = str.replace(/\>/g, '&gt;');
    str = str.replace(/\n/g, '<br/>');
    str = str.replace(/\[em_([0-9]*)\]/g, '<img src="arclist/$1.gif" border="0" />');
    return str;
};