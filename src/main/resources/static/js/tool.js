

//全局后台地址获取
var getUrl = function () {
    return "http://10.4.102.51:9001/";
};


//提示方式
var alertmsg = function(msg){
    layui.use('layer', function(){
        var layer = layui.layer;
        if(msg==="error"){
            layer.msg('发生了点小问题，稍后再试~');
        }else if(msg.length >=50){
            alert(msg);
        }else{
            layer.msg(msg);
        }
    });
};

