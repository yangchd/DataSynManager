//跟同步表配置有关的

//数据源界面跳转加载
var SynTableInterFace = function () {
    $('#SynTablePanelOnBtn').on('click', function () {
        $('#SynTablePanel').css("display", "block");
    });
    $('#SynTablePanelCloseBtn').on('click', function () {
        ColumnPanelClose();
    });
    //列修改面板
    $('#columnEditPanelClose').on('click', function () {
        ColumnPanelClose();
    });
};

var ColumnPanelClose = function () {
    $('#SynTableColumn').empty();
    $('#columnEdit').empty();
    $('#SynTablePanel').css("display", "none");
    $('#columnPanel').css("display", "none");
};

//联动加载
var LinkAction = function () {
    //数据库和表选择联动
    changeTable($('#datafrom'), $('#tablefrom'));
    changeTable($('#datato'), $('#tableto'));

    //同步表TO的列联动
    changeColumnTo($('#datato'), $('#tableto'));

    //left join功能
    addLeftJoin($('#leftjoinPanel'),$('#tablefrom'));
};

//本界面使用的参数

//增加一个记录列的
var dataToColumn = "";

//所有来源列
var allcolumnfrom = "";

var datatablefrom = "";   //选中库里面所有的表
var tablechose = "";    //已选中的所有表
var columnchose = "";   //所有选中表中的列
var joinnum = 0;        //关联表的个数

//清空leftjoin信息
var emptyleftjoin = function () {
    joinnum = 0;
    columnchose = "";
    tablechose = "";
    datatablefrom = "";
    $('#leftjoinPanel').empty();
};


/**
 * 根据数据源选择框动态获取表
 * @param datasource    数据源节点
 * @param table         表节点
 */
var changeTable = function (datasource, table) {
    datasource.on('change', function () {
        var data = {
            pk_datasource: datasource.val()
        };
        $.ajax({
            type: 'get',
            data: data,
            url: getUrl() + "dataSyn/getTables",
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

/**
 * 根据库和表获取表的所有列
 * @param datato
 * @param tableto
 */
var changeColumnTo = function (datato, tableto) {
    tableto.on('change', function () {
        var data = {
            pk_datasource: datato.val(),
            tablename: tableto.val()
        };
        $.ajax({
            type: 'get',
            data: data,
            url: getUrl() + "dataSyn/getColumnName",
            success: function (result) {
                dataToColumn = "";
                for (var i = 0; i < result.data.length; i++) {
                    dataToColumn += result.data[i].column_name+",";
                }
                if("" !== dataToColumn)dataToColumn = dataToColumn.substring(0,dataToColumn.length-1);
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};

//根据库和表获取所有列，左连接使用
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
            url: getUrl() + "dataSyn/getColumnName",
            success: function (result) {
                emptyoption(columnfrom);
                for (var i = 0; i < result.data.length; i++) {
                    columnfrom.append('<option>' + tablename + '.' + result.data[i].column_name + '</option>');
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    })
};


/**
 * 左连接有关能耐加载
 * @param node 添加left join的节点
 * @param tablefrom 从哪个节点的值开始进行左连接
 */
var addLeftJoin = function (node,tablefrom) {
    $('#leftjoinbtn').on('click', function () {
        //先确定已经选中的所有左表
        if (joinnum === 0) {
            tablechose = '<option>' + tablefrom.val() + '</option>';
        } else {
            tablechose = '<option>' + tablefrom.val() + '</option>';
            var haschose = tablefrom.val();//已关联的所有表 ，分割
            for (var i = 1; i < joinnum + 1; i++) {
                //如果没有关联过这个表，则添加
                if (haschose.indexOf($('#joinright' + i).val()) < 0) {
                    haschose += "," + $('#joinright' + i).val();
                    tablechose += '<option>' + $('#joinright' + i).val() + '</option>';
                }
            }
        }
        //添加DOM元素
        joinnum++;
        node.append(
            '<div class="col-sm-12" style="margin-bottom: 6px;">' +
            '<div class="col-sm-2"><select class="form-control" id="joinleft' + joinnum + '"></select></div>' +
            '<div class="col-sm-2"><p class="leftjoinfont">left join</p></div>' +
            '<div class="col-sm-2"><select class="form-control" id="joinright' + joinnum + '"></select></div>' +
            '<div class="col-sm-1"><p class="leftjoinfont">on</p></div>' +
            '<div class="col-sm-2"><select class="form-control" id="columnleft' + joinnum + '"></select></div>' +
            '<div class="col-sm-1"><p class="leftjoinfont">=</p></div>' +
            '<div class="col-sm-2"><select class="form-control" id="columnright' + joinnum + '"></select></div>' +
            '</div>');
        //然后给每个输入框添加数据
        emptyoption($('#joinleft' + joinnum));
        emptyoption($('#joinright' + joinnum));
        $('#joinleft' + joinnum).append(tablechose);
        $('#joinright' + joinnum).append(datatablefrom);

        //添加联动操作
        changeColumnFrom($('#datafrom'), $('#joinleft' + joinnum), $('#columnleft' + joinnum));
        changeColumnFrom($('#datafrom'), $('#joinright' + joinnum), $('#columnright' + joinnum));
    });
    //重置按钮功能
    $('#resetleftjoin').on('click', function () {
        joinnum = 0;
        $('#leftjoinPanel').empty();
    });
    //确定按钮功能
    $('#leftjoinmake').on('click', function () {
        //加载所有选项到匹配里面
        //获取所有表
        var alltablesfrom = $('#tablefrom').val();
        for (var j = 1; j < joinnum + 1; j++) {
            alltablesfrom += "," + $('#joinright' + j).val();
        }
        if(alltablesfrom === null || "" === alltablesfrom){
            alertmsg("您还没选择任何表！");
            return;
        }
        var tablessplit = alltablesfrom.split(",");
        allcolumnfrom = "";
        for (var m = 0; m < tablessplit.length; m++) {
            var data = {
                pk_datasource: $('#datafrom').val(),
                tablename: tablessplit[m]
            };
            $.ajax({
                type: 'get',
                data: data,
                async: false,
                url: getUrl() + "dataSyn/getColumnName",
                success: function (result) {
                    if(result.retflag === "0"){
                        for (var i = 0; i < result.data.length; i++) {
                            allcolumnfrom += tablessplit[m] + '.' + result.data[i].column_name+",";
                        }
                        if("" !== allcolumnfrom)allcolumnfrom = allcolumnfrom.substring(0,allcolumnfrom.length-1);
                        loadColumnRelation($('#SynTableColumn'),dataToColumn,allcolumnfrom,$('#SynTableColumnPanel'));
                    }else{
                        alertmsg(result.msg)
                    }
                },
                error: function (result) {
                    alertmsg(result.msg)
                }
            })
        }
    })
};


/**
 * 列匹配模块加载
 * column 加载到的节点
 * allcolumn 列
 * allcolumnfrom 来源列
 * panel 要显示的panel 没有则传 column
 * relation 根据这个，动态控制列匹配
 */
var loadColumnRelation = function(column,allcolumn,allcolumnfrom,panel,relation){
    if(allcolumn === ""){
        alertmsg("还没有选择同步表！");
        return;
    }
    column.empty();
    var coltos = allcolumn.split(",");
    for(var i=0;i<coltos.length;i++){
        column.append(
            '<div class="col-sm-6">' +
            '<div class="layui-form-item">' +
            '<label class="layui-form-label">列名</label>' +
            '<div class="layui-input-block">' +
            '<input type="text" id="' + coltos[i] + '" class="form-control columnto" value="' + coltos[i] + '">' +
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
    //加载所有选项到匹配里面

    emptyoption($('.columnfrom'));
    if(allcolumnfrom !== null && allcolumnfrom !== ""){
        var froms = allcolumnfrom.split(",");
        for(var m=0;m<froms.length;m++){
            $('.columnfrom').append('<option>' + froms[m] + '</option>')
        }
    }

    if(null !== relation && "" !== relation && typeof (relation) !== "undefined"){
        //当有relation的时候，动态匹配
        var relajson = eval('(' + relation + ')');
        var fromnode = $('.columnfrom');
        //对每一列进行操作
        for(var k=0;k<coltos.length;k++){
            //先根据relation找出option的需要，然后设置选中
            var index = fromnode[k].length;
            var colvalue = relajson[coltos[k]];
            for(var j=0;j<index;j++){
                if(colvalue === fromnode[k][j].value){
                    fromnode[k][j].selected="selected";
                }
            }
        }
    }

    //模块和列全部显示
    panel.css("display", "block");
    column.css("display", "block");
};

//保存配置按钮
var saveDataSynBtn = function () {
    //创建时保存
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
        for (var j = 1; j < joinnum + 1; j++) {
            fromtables += "," + $('#joinright' + j).val();
            tablesrelation += $('#columnleft' + j).val() + " = " + $('#columnright' + j).val() + ",";
        }
        tablesrelation = tablesrelation.substring(0, tablesrelation.length - 1);

        var data = {
            pk_datafrom: $('#datafrom').val(),
            pk_datato: $('#datato').val(),
            tablename: $('#tableto').val(),
            fromtables: fromtables,
            relation: JSON.stringify(relation),
            tablesrelation: tablesrelation,
            allcolumnfrom:allcolumnfrom
        };
        $.ajax({
            type: "get",
            data: data,
            url: getUrl() + "dataSyn/saveDataSynTable",
            success: function (result) {
                if(result.retflag === "0"){
                    DataSynStatus($('#datasyntable'));
                    ColumnPanelClose();
                    alertmsg(result.msg)
                }else{
                    alertmsg(result.msg)
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });

    //修改时保存
    $('#saveColumnEdit').on('click', function () {
        //列关系获取
        var columnto = $('.columnto');
        var columnfrom = $('.columnfrom');
        var relation = {};
        for (var i = 0; i < columnto.length; i++) {
            relation[$(columnto[i]).val()] = $(columnfrom[i]).val();
        }
        //来源表获取

        var data = {
            pk_table:$('#pk_table').text(),
            relation: JSON.stringify(relation)
        };
        $.ajax({
            type: "get",
            data: data,
            url: getUrl() + "dataSyn/updateSynTable",
            success: function (result) {
                if(result.retflag === "0"){
                    DataSynStatus($('#datasyntable'));
                    ColumnPanelClose();
                    alertmsg(result.msg)
                }else{
                    alertmsg(result.msg)
                }
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};