Ext.define('yikuaiju.view.main.MenuBar', {
    extend : 'Ext.tree.Panel',
    alias : 'widget.yikuaijumenubar',
    requires: [
//               'yikuaiju.controller.main.MainViewController',
               'yikuaiju.store.main.MenuBar'
           ],
    controller: 'mainviewcontroller',
    border : false,
    split : true,
    useArrows: true,
    title : '功能导航',
    width : 220,
    stateful : true,
    margins : '2 0 0 0',
    collapsible : true,
    animCollapse : true,
    rootVisible : false,
    store: 'yikuaiju.store.main.MenuBar',
    listeners: {
//        itemclick: 'onClickMenu'
    }
});