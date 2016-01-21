/*
YUI 3.14.1 (build 63049cb)
Copyright 2013 Yahoo! Inc. All rights reserved.
Licensed under the BSD License.
http://yuilibrary.com/license/
*/

if (typeof __coverage__ === 'undefined') { __coverage__ = {}; }
if (!__coverage__['build/scrollview-list/scrollview-list.js']) {
   __coverage__['build/scrollview-list/scrollview-list.js'] = {"path":"build/scrollview-list/scrollview-list.js","s":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0,"10":0,"11":0,"12":0,"13":0,"14":0,"15":0,"16":0,"17":0,"18":0,"19":0,"20":0,"21":0},"b":{"1":[0,0],"2":[0,0]},"f":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0},"fnMap":{"1":{"name":"(anonymous_1)","line":1,"loc":{"start":{"line":1,"column":27},"end":{"line":1,"column":46}}},"2":{"name":"ListPlugin","line":24,"loc":{"start":{"line":24,"column":0},"end":{"line":24,"column":22}}},"3":{"name":"(anonymous_3)","line":81,"loc":{"start":{"line":81,"column":17},"end":{"line":81,"column":28}}},"4":{"name":"(anonymous_4)","line":86,"loc":{"start":{"line":86,"column":23},"end":{"line":86,"column":34}}},"5":{"name":"(anonymous_5)","line":98,"loc":{"start":{"line":98,"column":28},"end":{"line":98,"column":43}}},"6":{"name":"(anonymous_6)","line":103,"loc":{"start":{"line":103,"column":28},"end":{"line":103,"column":43}}}},"statementMap":{"1":{"start":{"line":1,"column":0},"end":{"line":130,"column":78}},"2":{"start":{"line":8,"column":0},"end":{"line":13,"column":14}},"3":{"start":{"line":24,"column":0},"end":{"line":26,"column":1}},"4":{"start":{"line":25,"column":4},"end":{"line":25,"column":61}},"5":{"start":{"line":37,"column":0},"end":{"line":37,"column":31}},"6":{"start":{"line":47,"column":0},"end":{"line":47,"column":23}},"7":{"start":{"line":57,"column":0},"end":{"line":72,"column":2}},"8":{"start":{"line":74,"column":0},"end":{"line":117,"column":3}},"9":{"start":{"line":82,"column":8},"end":{"line":82,"column":36}},"10":{"start":{"line":83,"column":8},"end":{"line":83,"column":62}},"11":{"start":{"line":87,"column":8},"end":{"line":114,"column":9}},"12":{"start":{"line":88,"column":12},"end":{"line":90,"column":19}},"13":{"start":{"line":92,"column":12},"end":{"line":113,"column":13}},"14":{"start":{"line":94,"column":16},"end":{"line":94,"column":40}},"15":{"start":{"line":95,"column":16},"end":{"line":95,"column":45}},"16":{"start":{"line":98,"column":16},"end":{"line":100,"column":19}},"17":{"start":{"line":99,"column":20},"end":{"line":99,"column":46}},"18":{"start":{"line":103,"column":16},"end":{"line":105,"column":19}},"19":{"start":{"line":104,"column":20},"end":{"line":104,"column":46}},"20":{"start":{"line":107,"column":16},"end":{"line":107,"column":45}},"21":{"start":{"line":112,"column":16},"end":{"line":112,"column":36}}},"branchMap":{"1":{"line":87,"type":"if","locations":[{"start":{"line":87,"column":8},"end":{"line":87,"column":8}},{"start":{"line":87,"column":8},"end":{"line":87,"column":8}}]},"2":{"line":92,"type":"if","locations":[{"start":{"line":92,"column":12},"end":{"line":92,"column":12}},{"start":{"line":92,"column":12},"end":{"line":92,"column":12}}]}},"code":["(function () { YUI.add('scrollview-list', function (Y, NAME) {","","/**"," * Provides a plugin, which adds support for a scroll indicator to ScrollView instances"," *"," * @module scrollview-list"," */","var getCN = Y.ClassNameManager.getClassName,","SCROLLVIEW = 'scrollview',","LIST_CLASS = getCN(SCROLLVIEW, 'list'),","ITEM_CLASS = getCN(SCROLLVIEW, 'item'),","CONTENT_BOX = \"contentBox\",","HOST = \"host\";","","/**"," * ScrollView plugin that adds class names to immediate descendant \"<li>\" to"," *  allow for easier styling through CSS"," *"," * @class ScrollViewList"," * @namespace Plugin"," * @extends Plugin.Base"," * @constructor"," */","function ListPlugin() {","    ListPlugin.superclass.constructor.apply(this, arguments);","}","","","/**"," * The identity of the plugin"," *"," * @property NAME"," * @type String"," * @default 'pluginList'"," * @static"," */","ListPlugin.NAME = 'pluginList';","","/**"," * The namespace on which the plugin will reside."," *"," * @property NS"," * @type String"," * @default 'list'"," * @static"," */","ListPlugin.NS = 'list';","","","/**"," * The default attribute configuration for the plugin"," *"," * @property ATTRS"," * @type Object"," * @static"," */","ListPlugin.ATTRS = {","","    /**","     * Specifies whether the list elements (the immediate <ul>'s and the","     *  immediate <li>'s inside those <ul>'s) have class names attached to","     *  them or not","     *","     * @attribute isAttached","     * @type boolean","     * @deprecated No real use for this attribute on the public API","     */","    isAttached: {","        value:false,","        validator: Y.Lang.isBoolean","    }","};","","Y.namespace(\"Plugin\").ScrollViewList = Y.extend(ListPlugin, Y.Plugin.Base, {","","    /**","     * Designated initializer","     *","     * @method initializer","     */","    initializer: function() {","        this._host = this.get(HOST);","        this.afterHostEvent(\"render\", this._addClassesToList);","    },","","    _addClassesToList: function() {","        if (!this.get('isAttached')) {","            var cb = this._host.get(CONTENT_BOX),","            ulList,","            liList;","","            if (cb.hasChildNodes()) {","                //get all direct descendants of the UL's that are directly under the content box.","                ulList = cb.all('> ul');","                liList = cb.all('> ul > li');","","                //go through the UL's and add the class","                ulList.each(function(list) {","                    list.addClass(LIST_CLASS);","                });","","                //go through LI's and add the class","                liList.each(function(item) {","                    item.addClass(ITEM_CLASS);","                });","","                this.set('isAttached', true);","","                // We need to call this again, since sv-list","                //  relies on the \"-vert\" class, to apply padding.","                //  [ 1st syncUI pass applies -vert, 2nd pass re-calcs dims ]","                this._host.syncUI();","            }","        }","    }","","});","","","","","","","","","","","","","}, '3.14.1', {\"requires\": [\"plugin\", \"classnamemanager\"], \"skinnable\": true});","","}());"]};
}
var __cov_cNdS07fVBXPjuYYA6XvSog = __coverage__['build/scrollview-list/scrollview-list.js'];
__cov_cNdS07fVBXPjuYYA6XvSog.s['1']++;YUI.add('scrollview-list',function(Y,NAME){__cov_cNdS07fVBXPjuYYA6XvSog.f['1']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['2']++;var getCN=Y.ClassNameManager.getClassName,SCROLLVIEW='scrollview',LIST_CLASS=getCN(SCROLLVIEW,'list'),ITEM_CLASS=getCN(SCROLLVIEW,'item'),CONTENT_BOX='contentBox',HOST='host';__cov_cNdS07fVBXPjuYYA6XvSog.s['3']++;function ListPlugin(){__cov_cNdS07fVBXPjuYYA6XvSog.f['2']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['4']++;ListPlugin.superclass.constructor.apply(this,arguments);}__cov_cNdS07fVBXPjuYYA6XvSog.s['5']++;ListPlugin.NAME='pluginList';__cov_cNdS07fVBXPjuYYA6XvSog.s['6']++;ListPlugin.NS='list';__cov_cNdS07fVBXPjuYYA6XvSog.s['7']++;ListPlugin.ATTRS={isAttached:{value:false,validator:Y.Lang.isBoolean}};__cov_cNdS07fVBXPjuYYA6XvSog.s['8']++;Y.namespace('Plugin').ScrollViewList=Y.extend(ListPlugin,Y.Plugin.Base,{initializer:function(){__cov_cNdS07fVBXPjuYYA6XvSog.f['3']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['9']++;this._host=this.get(HOST);__cov_cNdS07fVBXPjuYYA6XvSog.s['10']++;this.afterHostEvent('render',this._addClassesToList);},_addClassesToList:function(){__cov_cNdS07fVBXPjuYYA6XvSog.f['4']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['11']++;if(!this.get('isAttached')){__cov_cNdS07fVBXPjuYYA6XvSog.b['1'][0]++;__cov_cNdS07fVBXPjuYYA6XvSog.s['12']++;var cb=this._host.get(CONTENT_BOX),ulList,liList;__cov_cNdS07fVBXPjuYYA6XvSog.s['13']++;if(cb.hasChildNodes()){__cov_cNdS07fVBXPjuYYA6XvSog.b['2'][0]++;__cov_cNdS07fVBXPjuYYA6XvSog.s['14']++;ulList=cb.all('> ul');__cov_cNdS07fVBXPjuYYA6XvSog.s['15']++;liList=cb.all('> ul > li');__cov_cNdS07fVBXPjuYYA6XvSog.s['16']++;ulList.each(function(list){__cov_cNdS07fVBXPjuYYA6XvSog.f['5']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['17']++;list.addClass(LIST_CLASS);});__cov_cNdS07fVBXPjuYYA6XvSog.s['18']++;liList.each(function(item){__cov_cNdS07fVBXPjuYYA6XvSog.f['6']++;__cov_cNdS07fVBXPjuYYA6XvSog.s['19']++;item.addClass(ITEM_CLASS);});__cov_cNdS07fVBXPjuYYA6XvSog.s['20']++;this.set('isAttached',true);__cov_cNdS07fVBXPjuYYA6XvSog.s['21']++;this._host.syncUI();}else{__cov_cNdS07fVBXPjuYYA6XvSog.b['2'][1]++;}}else{__cov_cNdS07fVBXPjuYYA6XvSog.b['1'][1]++;}}});},'3.14.1',{'requires':['plugin','classnamemanager'],'skinnable':true});
