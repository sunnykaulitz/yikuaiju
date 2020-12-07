Ext.define('yikuaiju.view.user.UserManagement', {
    extend : 'Ext.panel.Panel',
    alias : 'widget.userManagement',
    requires: [

//               'lsdreport.controller.callreport.CallreportMainController',
//               'yikuaiju.view.callreport.QueryForm',
//               'yikuaiju.view.callreport.CallreportTab'
           ],
//    controller: 'callreportmaincontroller',
    layout : {
        type : 'border'
    },
    items : [ /*{
        xtype : 'queryform',
        region: 'north'
    }, {
        xtype: 'callreporttab',
        region : 'center'
    } */],
    listeners: {
//        activate: 'activate'
    }
});
