//获取后台地址
var getUrl = function () {
    //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
    var curWwwPath=window.document.location.href;
    //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
    var pathName=window.document.location.pathname;
    var pos=curWwwPath.indexOf(pathName);
    //获取主机地址，如： http://localhost:8083
    return curWwwPath.substring(0, pos)+"/";

    // return "http://localhost:9001/";
};

//提示方式
var alertmsg = function (msg) {
    layui.use('layer', function () {
        var layer = layui.layer;
        if (msg === "error") {
            layer.msg('发生了点小问题，稍后再试~');
        } else if (msg !== "" && "undefined"!==msg && msg.length >= 20) {
            alert(msg);
        } else {
            layer.msg(msg);
        }
    });
};

/**
 * 选择框节点清空函数
 * @param node
 */
var emptyoption = function (node) {
    node.empty();
    node.append('<option></option>');
};

/**
 * 选择框生成函数
 * @param id            选择框id
 * @param ops           选择框内容
 * @param selectValue   选择框选择属性
 */
var createSelect = function (id,ops,selectValue) {
    var obj = document.getElementById(id);
    obj.options.length = 0;
    for(var j=0;j<ops.length;j++){
        obj.add(new Option(ops[j],ops[j]));
        if(ops[j] === selectValue)obj.options[j].selected = true;
    }
};

//同步运行状态加载
var DataSynStatus = function (node) {
    $.ajax({
        type: "get",
        url: getUrl() + "dataSyn/getDataSynStatus",
        success: function (result) {
            if (result.retflag === "0") {
                loadDataSyn(node, result.data);
            } else {
                alertmsg(result.msg)
            }
        },
        error: function (result) {
            alertmsg(result.msg)
        }
    })
};

/**
 * 同步状态加载函数
 * @param node 加载的节点
 * @param list 加载的数据
 */
var loadDataSyn = function (node, list) {
    node.empty();
    node.append('<table class="table table-hover">' +
        '<tbody class="datasynbody">' +
        '</tbody>' +
        '</table>');
    node = node.find(".datasynbody");
    node.append('<tr>' +
        '<th>目标表</th>' +
        '<th>上次同步时间</th>' +
        '<th>上次同步耗时</th>' +
        '<th>选项</th>' +
        '<th>开关</th>' +
        '<th>备注信息</th>' +
        '</tr>');
    for (var i = 0; i < list.length; i++) {
        var lasttime = (list[i].lasttime===null)?"":list[i].lasttime;
        var timecost = (list[i].timecost===null)?"":list[i].timecost;
        var datasynmsg = (list[i].datasynmsg===null)?"":list[i].datasynmsg;
        node.append('<tr>' +
            '<td style="display: none">' + list[i].pk_sync + '</td>' +
            '<td>' + list[i].tablename + '</td>' +
            '<td>' + lasttime + '</td>' +
            '<td>' + timecost + '</td>' +
            //选项按钮
            '<td>'+
            '<a class="columnEditBtn">编辑</a>' +
            '<a class="columnDeleteBtn" style="margin-left: 5px;">删除</a>' +
            '</td>' +
            '<td><input id="' + list[i].pk_sync + '" name="status" type="checkbox" data-size="mini"></td>' +
            '<td nowrap="nowrap"><div style="max-width: 200px;overflow: auto">' + datasynmsg + '</div></td>' +
            '</tr>');
        bootstrapswitch(list[i].pk_sync, list[i].flag);
    }

    //编辑按钮
    $('.columnEditBtn').on('click',function () {
        //当前行的主键
        var pk = $(this).parent().parent().children().eq(0).html();
        if(pk === "" || pk === null)return;
        var data = {
            pk_sync:pk
        };
        $.ajax({
            type: "get",
            data: data,
            url: getUrl() + "dataSyn/getDataSyn",
            success: function (result) {
                if(result.retflag === "0"){
                    loadColumnRelation($('#columnEdit'),result.data[0].allcolumn,result.data[0].allcolumnfrom,$('#columnPanel'),result.data[0].relation);
                    $('#pk_table').empty();
                    $('#pk_table').html(result.data[0].pk_table);
                    $('#wherevalueEdit').css("display","block");
                    $('#wherevaluebyedit').val(result.data[0].wherevalue);
                }else{
                    alertmsg(result.msg)
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });

    //删除按钮
    $('.columnDeleteBtn').on('click',function () {
        //当前行的主键
        var pk = $(this).parent().parent().children().eq(0).html();
        if(pk === "" || pk === null)return;
        var data = {
            pk_sync:pk
        };
        $.ajax({
            type: "get",
            data: data,
            url: getUrl() + "dataSyn/deleteDataSyn",
            success: function (result) {
                if(result.retflag === "0"){
                    DataSynStatus($('#datasyntable'));
                    alertmsg(result.msg);
                }else{
                    alertmsg(result.msg);
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};


//开关控制
var bootstrapswitch = function (id, flag) {
    var stateflag = false;
    if (flag === "true") stateflag = true;
    var node = $('#' + id);
    node.bootstrapSwitch({
        state: stateflag,
        onText: "开启",
        offText: "关闭",
        size: "mini",
        onSwitchChange: function (event, state) {
            var setflag = "";
            if (state === true) {//打开动作
                setflag = "true";
            } else {//关闭动作
                setflag = "false";
            }
            var data = {
                pk_sync: id,
                flag: setflag
            };
            $.ajax({
                type: "get",
                data: data,
                url: getUrl() + "dataSyn/saveDataSyn",
                success: function (result) {
                    if(result.retflag === "0"){
                        alertmsg("操作成功！");
                    }else{
                        alertmsg(result.msg)
                    }
                },
                error: function (result) {
                    alertmsg(result.msg)
                }
            })
        }
    });
};

//所有面板的开关
var AllPanelSwitch = function () {
    //数据源面板
    dataSourceInterFace();

    //同步表面板
    SynTableInterFace();

};

//界面初始化数据
var loadAllData = function () {
    //数据源状态加载
    loadDataSourceStatus($('.dataSource-status'));

    //数据源选择框加载
    loadDataSourceOption($('#datafrom'));
    loadDataSourceOption($('#datato'));

    //联动
    LinkAction();

    DataSynStatus($('#datasyntable'));

};

window.onload = function () {

    //所有面板的开关
    AllPanelSwitch();

    //加载初始化数据
    loadAllData();

    /*************************功能性操作分割线****************************/

    //测试连接、保存数据源按钮
    conTest();

    //保存配置
    saveDataSynBtn();

    //启动同步按钮
    startDataSyn();

};

//启动同步按钮
var startDataSyn = function () {
    $('#startDataSynBtn').on('click', function () {
        $.ajax({
            type: "get",
            url: getUrl() + "dataSyn/startSyn",
            success: function (result) {
                //同步成功重新加载页面
                DataSynStatus($('#datasyntable'));
                alertmsg(result.msg)
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};


