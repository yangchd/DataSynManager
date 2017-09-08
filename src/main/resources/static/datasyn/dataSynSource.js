/**
 * 数据源有关的方法
 */

//数据源界面跳转加载
var dataSourceInterFace = function () {
    $('#dataSourcePanelOnBtn').on('click', function () {
        $('#DataSourcePanel').css("display", "block");
    });
    $('#DataSourcePanelCloseBtn').on('click', function () {
        $('#DataSourcePanel').css("display", "none");
        closeDataSourceBody();
    });
    //生成数据源编辑面板，按参数
    $('#DataSourceBodyBtn').on('click', function () {
        openDataSourceBody(null);
    });
    $('#dataSourceBodyCloseBtn').on('click', function () {
        closeDataSourceBody();
    });
    $('#DataSourcePanelReLoadBtn').on('click', function () {
        reloadDataSource();
    });
};

//数据源Body打开方法
var openDataSourceBody = function (data) {
    if (data !== null && "undefined"!==typeof(data)) {
        $('#pk_datasource').html(data.pk_datasource);
        $('#dataSourceName').val(data.datasourcename);
        $('#url_ip').val(data.url_ip);
        $('#url_port').val(data.url_port);
        $('#basename').val(data.basename);
        $('#username').val(data.username);
        $('#password').val(data.password);
        var ops = ['com.mysql.jdbc.Driver','oracle.jdbc.driver.OracleDriver',
            'com.microsoft.sqlserver.jdbc.SQLServerDriver','com.microsoft.jdbc.sqlserver.SQLServerDriver'];
        createSelect("driver",ops,data.driver);
    }
    $('#DataSourceBody').css("display", "block");
};
//数据源Body关闭方法
var closeDataSourceBody = function () {
    $('#pk_datasource').html("");
    $('#dataSourceName').val("");
    $('#url_ip').val("");
    $('#url_port').val("");
    $('#basename').val("");
    $('#username').val("");
    $('#password').val("");
    $('#driver').empty();
    var ops = ['com.mysql.jdbc.Driver','oracle.jdbc.driver.OracleDriver',
        'com.microsoft.sqlserver.jdbc.SQLServerDriver','com.microsoft.jdbc.sqlserver.SQLServerDriver'];
    createSelect("driver",ops,'com.mysql.jdbc.Driver');
    $('#DataSourceBody').css("display", "none");
};

//界面重新加载方法
var reloadDataSource = function (listnode, optionnode) {
    if(listnode !==null && "undefined" !== typeof(listnode)){
        loadDataSourceStatus(listnode);
    }else{
        loadDataSourceStatus($('.dataSource-status'));
    }
    if(optionnode !==null && "undefined" !== typeof(optionnode)){
        loadDataSourceOption(optionnode);
    }else{
        loadDataSourceOption($('#datafrom'));
        loadDataSourceOption($('#datato'));
    }
    closeDataSourceBody();
};

//数据源选择框加载方法
var loadDataSourceOption = function (node) {
    $.ajax({
        type: "get",
        url: getUrl() + "dataSyn/getDataSource",
        success: function (result) {
            if (result.retflag === "0") {
                emptyoption(node);
                for (var j = 0; j < result.data.length; j++) {
                    node.append('<option value=' + result.data[j].pk_datasource + '>' + result.data[j].datasourcename + '</option>');
                }
            } else {
                alertmsg(result.msg)
            }
        },
        error: function (result) {
            alertmsg(result.msg)
        }
    })
};

//数据源状态加载
var loadDataSourceStatus = function (node) {
    $.ajax({
        type: "get",
        url: getUrl() + "dataSyn/getDataSource",
        success: function (result) {
            if (result.retflag === "0") {
                node.empty();
                loadDataSourceList(node, result.data);
            } else {
                alertmsg(result.msg)
            }
        },
        error: function (result) {
            alertmsg(result.msg)
        }
    })
};

//数据源状态列表加载方法
var loadDataSourceList = function (node, list) {
    node.empty();
    node.append('<table class="table table-hover">' +
        '<tbody class="dataSourceTable">' +
        '</tbody>' +
        '</table>');
    node = node.find(".dataSourceTable");
    node.append('<tr>' +
        '<th>数据源名称</th>' +
        '<th>url</th>' +
        '<th>用户</th>' +
        '<th>选项</th>' +
        '</tr>');
    for (var i = 0; i < list.length; i++) {
        node.append('<tr>' +
            '<td style="display: none">' + list[i].pk_datasource + '</td>' +
            '<td>' + list[i].datasourcename + '</td>' +
            '<td>' + list[i].url + '</td>' +
            '<td>' + list[i].username + '</td>' +
            '<td style="display: none">' + list[i].driver + '</td>' +
            '<td style="display: none">' + list[i].password + '</td>' +
            '<td style="display: none">' + list[i].basename + '</td>' +
            '<td style="display: none">' + list[i].url_ip + '</td>' +
            '<td style="display: none">' + list[i].url_port + '</td>' +
            '<td style="display: none">' + list[i].pk_datasource + '</td>' +
            //选项按钮
            '<td>' +
            '<a class="testConBtn">测试连接</a>' +
            '<a class="dataSourceEditBtn" style="margin-left: 5px;">编辑</a>' +
            '<a class="dataSourceDeleteBtn" style="margin-left: 5px;">删除</a>' +
            '</td>' +
            '</tr>');
        bootstrapswitch(list[i].pk_sync, list[i].flag);
    }
    //测试连接按钮
    $('.testConBtn').on('click', function () {
        var driver = $(this).parent().parent().children().eq(4).html();
        var password = $(this).parent().parent().children().eq(5).html();
        var url = $(this).parent().parent().children().eq(2).html();
        var username = $(this).parent().parent().children().eq(3).html();
        testConnection(driver, url, username, password);
    });
    //编辑按钮
    $('.dataSourceEditBtn').on('click', function () {
        var data = {
            pk_datasource:$(this).parent().parent().children().eq(9).html(),
            datasourcename: $(this).parent().parent().children().eq(1).html(),
            username: $(this).parent().parent().children().eq(3).html(),
            driver: $(this).parent().parent().children().eq(4).html(),
            password: $(this).parent().parent().children().eq(5).html(),
            basename: $(this).parent().parent().children().eq(6).html(),
            url_ip: $(this).parent().parent().children().eq(7).html(),
            url_port: $(this).parent().parent().children().eq(8).html()
        };
        openDataSourceBody(data);
    });
    //删除按钮
    $('.dataSourceDeleteBtn').on('click', function () {
        var pk = $(this).parent().parent().children().eq(0).html();
        var data = {
            pk_datasource: pk
        };
        $.ajax({
            type: "get",
            url: getUrl() + "dataSyn/deleteDataSource",
            data: data,
            success: function (result) {
                if (result.retflag === "0") {
                    reloadDataSource();
                }
                alertmsg(result.msg);
            },
            error: function (result) {
                alertmsg(result);
            }
        })
    })
};

//连接测试,并保存数据源
var conTest = function () {
    $('#saveDataSourceBtn').on('click', function () {
        var pk_datasource = $('#pk_datasource').html();
        var name = $('#dataSourceName').val();
        var ip = $('#url_ip').val();
        var port = $('#url_port').val();
        var basename = $('#basename').val();
        var driver = $('#driver').val();
        var url = "";
        if (driver === "com.mysql.jdbc.Driver") {
            url = "jdbc:mysql://" + ip + ":" + port + "/" + basename;
        } else if (driver === "oracle.jdbc.driver.OracleDriver") {
            url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + basename;
        } else if (driver === "oracle.jdbc.driver.OracleDriver") {
            url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + basename;
        } else if (driver === "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            || driver === "com.microsoft.jdbc.sqlserver.SQLServerDriver") {
            url = "jdbc:sqlserver://" + ip + ":" + port + ";DatabaseName=" + basename;
        }
        var data = {
            pk_datasource:pk_datasource,
            datasourcename: name,
            url: url,
            username: $('#username').val(),
            password: $('#password').val(),
            basename: basename,
            driver: driver,
            url_ip: ip,
            url_port: port
        };
        $.ajax({
            type: "get",
            url: getUrl() + "dataSyn/saveDataSource",
            timeout:1000,
            data: data,
            success: function (result) {
                if(result.retflag === "0"){
                    reloadDataSource();
                }
                alertmsg(result.msg)
            },
            error: function (result) {
                alertmsg(result.msg)
            }
        })
    });
};

//单纯连接测试
var testConnection = function (driver, url, username, password) {
    var data = {
        driver: driver,
        url: url,
        username: username,
        password: password
    };
    $.ajax({
        type: "get",
        url: getUrl() + "dataSyn/testConnection",
        timeout:1000,
        data: data,
        success: function (result) {
            alertmsg(result.msg);
        },
        error: function (result) {
            alertmsg(result.statusText);
        }
    })
};