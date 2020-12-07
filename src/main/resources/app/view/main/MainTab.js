Ext.define('yikuaiju.view.main.MainTab', {
    extend : 'Ext.TabPanel',
    alias : 'widget.maintab',
    requires: [
               'yikuaiju.view.main.MainForm',
               'yikuaiju.view.user.UserManagement'
           ],
    border : false,
    margins : '2 0 0 0',
    deferredRender : false,
    autoDestroy : false,
    activeTab : 0,
    items : [ {
        xtype : 'mainform',
        closable: true,
        title : '主界面'
    } ]
});