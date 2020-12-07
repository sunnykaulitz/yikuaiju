Ext.define("yikuaiju.view.Viewport", {
    extend : "Ext.container.Viewport",
    xtype: 'mainview',
    requires : [ 'yikuaiju.view.main.MenuBar', 'yikuaiju.view.main.MainTab','Ext.ux.statusbar.StatusBar' ],
    layout : 'border',
    items : [ {
        region : 'north',
        xtype : 'container',
        height : 64,
        id : 'app-header',
        layout : {
            type : 'hbox',
            align : 'middle'
        },
        defaults : {
            xtype : 'component'
        },
        items : [ {
            html : '<img src = "static/images/LOGO.png" width="124" height="54" />',// 修改top
            width : 160
        }, {
            id : 'app-header-title',
            html : '&nbsp;&nbsp;&nbsp;&nbsp;'+appName,
            style : 'font-size:25px;font-weight:bold;color:#FFFFFF;',
            width : 300
        }, {
            html : '欢迎您，' + userName,
            style : 'text-align:center;font-size:14px;font-weight:bold;color:#FFFFFF;',
            flex : 1
        }, {
            width : 120,
            xtype : 'button',
            text : '个人中心',
            icon : 'static/images/icons/user.png',
            menu : [ '-', {
                text : '安全退出',
                handler : function() {
                    top.location.href = basePath + 'hello/logout';
                }
            } ]
        } ]
    }, {
        region : 'west',
        padding : '2px 1px 2px 2px',
        xtype : 'menubar'
    }, {
        region : 'center',
        xtype : 'maintab'
    }, {
        region : 'south',
        border : false,
        items : [ {
            xtype : 'statusbar',
            border : false,
            text : '',
            style : 'background:#3892D3;',
            defaults : {
                style : 'color:#FFFFFF;font-weight:bold;'
            },
            items : [ '->', '一块聚', '-', '©2020', '->', '->' ]
        } ]
    } ]
});