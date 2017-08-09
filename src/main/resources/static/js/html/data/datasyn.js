
var datatablefrom="";   //选中库里面所有的表
var tablechose = "";    //已选中的所有表
var columnchose = "";   //所有选中表中的列
var joinnum = 0;        //关联表的个数
//清空leftjoin信息
var emptyleftjoin = function () {
    joinnum = 0;
    columnchose = "";
    tablechose = "";
    datatablefrom = "";
    $('#leftjoin').empty();
};



//清空选择节点，单独写函数，方便修改
var emptyoption = function (node) {
    node.empty();
    node.append('<option></option>');
};

//连接测试,并保存数据源
var conTest = function () {
    $('#saveDataSourceBtn').on('click', function () {
        var driver = $('#driver').val();
        var url = "";
        var basename = $('#basename').val();
        if (driver === "com.mysql.jdbc.Driver") {
            url = "jdbc:mysql://" + $('#url_ip').val() + ":" + $('#url_port').val() + "/" + basename;
        } else if (driver === "oracle.jdbc.driver.OracleDriver") {
            url = "jdbc:oracle:thin:@" + $('#url_ip').val() + ":" + $('#url_port').val() + ":" + basename;
        }
        var data = {
            url: url,
            driver: driver,
            basename: basename,
            username: $('#username').val(),
            password: $('#password').val()
        };
        $.ajax({
            type: "get",
            url: getUrl() + "datasyn/savedatasource",
            data: data,
            success: function (result) {
                alertmsg(result.msg)
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};

//数据库选择框
var getDataSource = function (node) {
    $.ajax({
        type: "get",
        url: getUrl() + "datasyn/getdatasource",
        success: function (result) {
            for (var i = 0; i < node.length; i++) {
                emptyoption(node[i]);
                for (var j = 0; j < result.data.length; j++) {
                    node[i].append('<option value=' + result.data[j].pk_datasource + '>' + result.data[j].url + '</option>');
                }
            }
        },
        error: function (result) {
            alertmsg(result.msg)
        }
    })
};

//数据源选择联动表
var changeTable = function (datasource, table) {
    datasource.on('change', function () {
        var data = {
            pk_datasource: datasource.val()
        };
        $.ajax({
            type: 'get',
            data: data,
            url: getUrl() + "datasyn/getalltable",
            success: function (result) {
                if (result.retflag === '0') {
                    //切换数据库清空left join
                    emptyleftjoin();

                    emptyoption(table);
                    datatablefrom = "";
                    for (var i = 0; i < result.data.length; i++) {
                        datatablefrom += '<option>' + result.data[i].table_name + '</option>';
                    }
                    table.append(datatablefrom);
                } else {
                    emptyoption(table);
                    alertmsg(result.msg);
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};

//根据表名联动列名
var changeColumnTo = function (datato, tableto, column) {
    tableto.on('change', function () {
        var data = {
            pk_datasource: datato.val(),
            tablename: tableto.val()
        };
        $.ajax({
            type: 'get',
            data: data,
            url: getUrl() + "datasyn/getcolumnname",
            success: function (result) {
                emptyoption(column);
                for (var i = 0; i < result.data.length; i++) {
                    column.append(
                        '<div class="col-sm-6">' +
                        '<div class="layui-form-item">' +
                        '<label class="layui-form-label">列名</label>' +
                        '<div class="layui-input-block">' +
                        '<input type="text" id="' + result.data[i].column_name + '" class="form-control columnto" value="' + result.data[i].column_name + '">' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '<div class="col-sm-6">' +
                        '<div class="layui-form-item">' +
                        '<label class="layui-form-label">匹配</label>' +
                        '<div class="layui-input-block">' +
                        '<select id="" class="form-control columnfrom">' +
                        '</select>' +
                        '</div>' +
                        '</div>' +
                        '</div>');
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};

//根据表名联动列名
var changeColumnFrom = function (datafrom, tablefrom, columnfrom) {
    tablefrom.on('change', function () {
        var tablename = tablefrom.val();
        var data = {
            pk_datasource: datafrom.val(),
            tablename: tablename
        };
        $.ajax({
            type: 'get',
            data: data,
            url: getUrl() + "datasyn/getcolumnname",
            success: function (result) {
                for (var j = 0; j < columnfrom.length; j++) {
                    emptyoption(columnfrom);
                    for (var i = 0; i < result.data.length; i++) {
                        columnfrom.append('<option>' + tablename + '.' + result.data[i].column_name + '</option>');
                    }
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};

//保存配置按钮
var saveDataSynBtn = function () {
    $('#datasynbtn').on('click', function () {

        //列关系获取
        var columnto = $('.columnto');
        var columnfrom = $('.columnfrom');
        var relation = {};
        for (var i = 0; i < columnto.length; i++) {
            relation[$(columnto[i]).val()] = $(columnfrom[i]).val();
        }
        //来源表获取
        var fromtables = $('#tablefrom').val();
        var tablesrelation = "";
        for(var j=1;j<joinnum+1;j++){
            fromtables += "," + $('#joinright'+j).val();
            tablesrelation += $('#columnleft'+j).val() + " = " + $('#columnright'+j).val() + ",";
        }
        tablesrelation = tablesrelation.substring(0,tablesrelation.length-1);

        var data = {
            pk_datafrom: $('#datafrom').val(),
            pk_datato: $('#datato').val(),
            tablename: $('#tableto').val(),
            fromtables: fromtables,
            relation: JSON.stringify(relation),
            tablesrelation: tablesrelation,
        };
        $.ajax({
            type: "get",
            data: data,
            url: getUrl() + "datasyn/savedatasyntable",
            success: function (result) {
                alertmsg(result.msg)
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};

//所有面板的开关
var switchBtn = function () {
    //数据源面板
    $('#dataSourcePanelBtn').on('click', function () {
        $('#createDataSource').css("display", "none");
    });
    $('#dataSourcePanelOnBtn').on('click', function () {
        $('#createDataSource').css("display", "block");
    });
    //数据同步配置
    $('#datasynPanelBtn').on('click', function () {
        $('#syntable').css("display", "none");
    });
    $('#dataSynOnBtn').on('click', function () {
        $('#syntable').css("display", "block");
    });
};

//开关控制
var bootstrapswitch = function (id, flag) {
    var stateflag = false;
    if(flag==="true")stateflag = true;

    var node = $('#'+id);
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
                pk_sync:id,
                flag:setflag
            };
            $.ajax({
                type:"get",
                data: data,
                url: getUrl() + "datasyn/savedatasyn",
                success: function (result) {
                    alertmsg(result.msg)
                },
                error: function (result) {
                    alertmsg(result.msg)
                }
            })
        }
    });
};

//同步运行状态加载
var DataSynStatus = function () {
    $.ajax({
        type: "get",
        url: getUrl() + "datasyn/getdatasynstatus",
        success: function (result) {
            if(result.retflag==="0"){
                $('#datasyntable').empty();
                $('#datasyntable').append('<tr>' +
                    '<th>目标表</th>' +
                    '<th>同步表</th>' +
                    '<th>上次同步时间</th>' +
                    '<th>上次同步耗时</th>' +
                    '<th>开关</th>' +
                    '<th>备注信息</th>' +
                    '</tr>');
                for (var i=0;i<result.data.length;i++){
                    $('#datasyntable').append('<tr>' +
                        '<td style="display: none">'+result.data[i].pk_sync+'</td>' +
                        '<td>'+result.data[i].tablename+'</td>' +
                        '<td>'+result.data[i].fromtables+'</td>' +
                        '<td>'+result.data[i].lasttime+'</td>' +
                        '<td>'+result.data[i].timecost+'</td>' +
                        '<td><input id="'+result.data[i].pk_sync+'" name="status" type="checkbox" data-size="mini"></td>' +
                        '<td nowrap="nowrap"><div style="max-width: 200px;overflow: auto">'+result.data[i].datasynmsg+'</div></td>' +
                        '</tr>');
                    bootstrapswitch(result.data[i].pk_sync,result.data[i].flag);
                }
            }else {
                alertmsg(result.msg)
            }
        },
        error: function (result) {
            alertmsg(result.msg)
        }
    })
};

//在添加来源表的时候，添加关联关系 left join
var addLeftJoin = function () {
    $('#leftjoinbtn').on('click', function () {
        //先确定已经选中的所有左表
        if(joinnum===0){
            tablechose = '<option>' + $('#tablefrom').val() + '</option>';
        }else{
            tablechose = '<option>' + $('#tablefrom').val() + '</option>';
            var haschose = $('#tablefrom').val();
            var newchose = "";
            for (var i=1;i<joinnum+1;i++){
                newchose = $('#joinright'+i).val();
                if(haschose.indexOf(newchose)<0){
                    haschose += ","+$('#joinright'+i).val();
                    tablechose += '<option>' + $('#joinright'+i).val() + '</option>';;
                }
            }
        }
        //添加DOM元素
        joinnum++;
        $('#leftjoin').append(
            '<div class="col-sm-12" style="margin-bottom: 6px;">'+
            '<div class="col-sm-2"><select class="form-control" id="joinleft'+joinnum+'"></select></div>'+
            '<div class="col-sm-2"><p class="leftjoinfont">left join</p></div>'+
            '<div class="col-sm-2"><select class="form-control" id="joinright'+joinnum+'"></select></div>'+
            '<div class="col-sm-1"><p class="leftjoinfont">on</p></div>'+
            '<div class="col-sm-2"><select class="form-control" id="columnleft'+joinnum+'"></select></div>'+
            '<div class="col-sm-1"><p class="leftjoinfont">=</p></div>'+
            '<div class="col-sm-2"><select class="form-control" id="columnright'+joinnum+'"></select></div>'+
            '</div>');
        //然后给每个输入框添加数据
        emptyoption($('#joinleft'+joinnum));
        emptyoption($('#joinright'+joinnum));
        $('#joinleft'+joinnum).append(tablechose);
        $('#joinright'+joinnum).append(datatablefrom);
        changeColumnFrom($('#datafrom'),$('#joinleft'+joinnum),$('#columnleft'+joinnum));
        changeColumnFrom($('#datafrom'),$('#joinright'+joinnum),$('#columnright'+joinnum));
    });
    $('#resetleftjoin').on('click', function () {
        joinnum = 0;
        $('#leftjoin').empty();
    });
    //
    $('#leftjoinmake').on('click', function () {
        //加载所有选项到匹配里面
        emptyoption($('.columnfrom'));

        var allcolumn = "";

        //获取所有表
        var alltablesfrom = $('#tablefrom').val();
        for(var j=1;j<joinnum+1;j++) {
            alltablesfrom += "," + $('#joinright' + j).val();
        }
        var tablessplit = alltablesfrom.split(",");
        for(var m=0;m<tablessplit.length;m++){
            var data = {
                pk_datasource: $('#datafrom').val(),
                tablename: tablessplit[m]
            };
            $.ajax({
                type: 'get',
                data: data,
                async: false,
                url: getUrl() + "datasyn/getcolumnname",
                success: function (result) {
                    for (var i = 0; i < result.data.length; i++) {
                        allcolumn += '<option>' + tablessplit[m] + '.' + result.data[i].column_name + '</option>';
                    }
                },
                error: function (result) {
                    alertmsg(result.msg)
                }
            })
        }
        $('.columnfrom').append(allcolumn);
    })
};

//启动同步按钮
var startDataSyn = function () {
    $('#startDataSynBtn').on('click',function () {
        $.ajax({
            type:"get",
            url: getUrl() + "datasyn/startdatasyn",
            success: function (result) {
                //同步成功重新加载页面
                DataSynStatus();
                alertmsg(result.msg)
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};

window.onload = function () {

    //启动同步按钮
    startDataSyn();

    //面板开关控制,用来打开和关闭面板
    switchBtn();

    //加载数据
    DataSynStatus();

    //测试连接、保存数据源按钮
    conTest();

    //数据源载入
    var node = [$('#datafrom'), $('#datato')];
    getDataSource(node);

    //数据库和表选择联动
    changeTable($('#datafrom'), $('#tablefrom'));

    changeTable($('#datato'), $('#tableto'));

    //left join功能
    addLeftJoin();

    //选择表以后，表和列名联动
    changeColumnTo($('#datato'), $('#tableto'), $('#column'));

    //保存配置
    saveDataSynBtn();

};