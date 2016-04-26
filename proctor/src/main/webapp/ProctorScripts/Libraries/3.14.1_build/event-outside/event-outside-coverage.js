/*
YUI 3.14.1 (build 63049cb)
Copyright 2013 Yahoo! Inc. All rights reserved.
Licensed under the BSD License.
http://yuilibrary.com/license/
*/

if (typeof __coverage__ === 'undefined') { __coverage__ = {}; }
if (!__coverage__['build/event-outside/event-outside.js']) {
   __coverage__['build/event-outside/event-outside.js'] = {"path":"build/event-outside/event-outside.js","s":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0,"10":0,"11":0,"12":0,"13":0,"14":0,"15":0,"16":0,"17":0,"18":0,"19":0},"b":{"1":[0,0],"2":[0,0],"3":[0,0],"4":[0,0]},"f":{"1":0,"2":0,"3":0,"4":0,"5":0,"6":0,"7":0,"8":0,"9":0,"10":0},"fnMap":{"1":{"name":"(anonymous_1)","line":1,"loc":{"start":{"line":1,"column":25},"end":{"line":1,"column":44}}},"2":{"name":"(anonymous_2)","line":64,"loc":{"start":{"line":64,"column":24},"end":{"line":64,"column":47}}},"3":{"name":"(anonymous_3)","line":69,"loc":{"start":{"line":69,"column":12},"end":{"line":69,"column":43}}},"4":{"name":"(anonymous_4)","line":70,"loc":{"start":{"line":70,"column":48},"end":{"line":70,"column":60}}},"5":{"name":"(anonymous_5)","line":78,"loc":{"start":{"line":78,"column":16},"end":{"line":78,"column":47}}},"6":{"name":"(anonymous_6)","line":82,"loc":{"start":{"line":82,"column":18},"end":{"line":82,"column":57}}},"7":{"name":"(anonymous_7)","line":83,"loc":{"start":{"line":83,"column":54},"end":{"line":83,"column":67}}},"8":{"name":"(anonymous_8)","line":90,"loc":{"start":{"line":90,"column":19},"end":{"line":90,"column":43}}},"9":{"name":"(anonymous_9)","line":91,"loc":{"start":{"line":91,"column":55},"end":{"line":91,"column":68}}},"10":{"name":"(anonymous_10)","line":102,"loc":{"start":{"line":102,"column":27},"end":{"line":102,"column":44}}}},"statementMap":{"1":{"start":{"line":1,"column":0},"end":{"line":107,"column":48}},"2":{"start":{"line":44,"column":0},"end":{"line":48,"column":6}},"3":{"start":{"line":64,"column":0},"end":{"line":99,"column":2}},"4":{"start":{"line":65,"column":4},"end":{"line":65,"column":39}},"5":{"start":{"line":67,"column":4},"end":{"line":95,"column":6}},"6":{"start":{"line":70,"column":12},"end":{"line":75,"column":21}},"7":{"start":{"line":71,"column":16},"end":{"line":74,"column":17}},"8":{"start":{"line":72,"column":20},"end":{"line":72,"column":43}},"9":{"start":{"line":73,"column":20},"end":{"line":73,"column":37}},"10":{"start":{"line":79,"column":12},"end":{"line":79,"column":32}},"11":{"start":{"line":83,"column":12},"end":{"line":87,"column":29}},"12":{"start":{"line":84,"column":16},"end":{"line":86,"column":17}},"13":{"start":{"line":85,"column":20},"end":{"line":85,"column":37}},"14":{"start":{"line":91,"column":12},"end":{"line":93,"column":19}},"15":{"start":{"line":92,"column":20},"end":{"line":92,"column":38}},"16":{"start":{"line":96,"column":4},"end":{"line":96,"column":42}},"17":{"start":{"line":98,"column":4},"end":{"line":98,"column":33}},"18":{"start":{"line":102,"column":0},"end":{"line":104,"column":3}},"19":{"start":{"line":103,"column":4},"end":{"line":103,"column":33}}},"branchMap":{"1":{"line":65,"type":"binary-expr","locations":[{"start":{"line":65,"column":11},"end":{"line":65,"column":15}},{"start":{"line":65,"column":20},"end":{"line":65,"column":37}}]},"2":{"line":71,"type":"if","locations":[{"start":{"line":71,"column":16},"end":{"line":71,"column":16}},{"start":{"line":71,"column":16},"end":{"line":71,"column":16}}]},"3":{"line":84,"type":"if","locations":[{"start":{"line":84,"column":16},"end":{"line":84,"column":16}},{"start":{"line":84,"column":16},"end":{"line":84,"column":16}}]},"4":{"line":91,"type":"binary-expr","locations":[{"start":{"line":91,"column":19},"end":{"line":91,"column":34}},{"start":{"line":91,"column":38},"end":{"line":93,"column":18}}]}},"code":["(function () { YUI.add('event-outside', function (Y, NAME) {","","/**"," * Outside events are synthetic DOM events that fire when a corresponding native"," * or synthetic DOM event occurs outside a bound element."," *"," * The following outside events are pre-defined by this module:"," * <ul>"," *   <li>blur</li>"," *   <li>change</li>"," *   <li>click</li>"," *   <li>dblclick</li>"," *   <li>focus</li>"," *   <li>keydown</li>"," *   <li>keypress</li>"," *   <li>keyup</li>"," *   <li>mousedown</li>"," *   <li>mousemove</li>"," *   <li>mouseout</li>"," *   <li>mouseover</li>"," *   <li>mouseup</li>"," *   <li>select</li>"," *   <li>submit</li>"," * </ul>"," *"," * Define new outside events with"," * <code>Y.Event.defineOutside(eventType);</code>."," * By default, the created synthetic event name will be the name of the event"," * with \"outside\" appended (e.g. \"click\" becomes \"clickoutside\"). If you want"," * a different name for the created Event, pass it as a second argument like so:"," * <code>Y.Event.defineOutside(eventType, \"yonderclick\")</code>."," *"," * This module was contributed by Brett Stimmerman, promoted from his"," * gallery-outside-events module at"," * http://yuilibrary.com/gallery/show/outside-events"," *"," * @module event"," * @submodule event-outside"," * @author brettstimmerman"," * @since 3.4.0"," */","","// Outside events are pre-defined for each of these native DOM events","var nativeEvents = [","        'blur', 'change', 'click', 'dblclick', 'focus', 'keydown', 'keypress',","        'keyup', 'mousedown', 'mousemove', 'mouseout', 'mouseover', 'mouseup',","        'select', 'submit'","    ];","","/**"," * Defines a new outside event to correspond with the given DOM event."," *"," * By default, the created synthetic event name will be the name of the event"," * with \"outside\" appended (e.g. \"click\" becomes \"clickoutside\"). If you want"," * a different name for the created Event, pass it as a second argument like so:"," * <code>Y.Event.defineOutside(eventType, \"yonderclick\")</code>."," *"," * @method defineOutside"," * @param {String} event DOM event"," * @param {String} name (optional) custom outside event name"," * @static"," * @for Event"," */","Y.Event.defineOutside = function (event, name) {","    name = name || (event + 'outside');","","    var config = {","","        on: function (node, sub, notifier) {","            sub.handle = Y.one('doc').on(event, function(e) {","                if (this.isOutside(node, e.target)) {","                    e.currentTarget = node;","                    notifier.fire(e);","                }","            }, this);","        },","","        detach: function (node, sub, notifier) {","            sub.handle.detach();","        },","","        delegate: function (node, sub, notifier, filter) {","            sub.handle = Y.one('doc').delegate(event, function (e) {","                if (this.isOutside(node, e.target)) {","                    notifier.fire(e);","                }","            }, filter, this);","        },","","        isOutside: function (node, target) {","            return target !== node && !target.ancestor(function (p) {","                    return p === node;","                });","        }","    };","    config.detachDelegate = config.detach;","","    Y.Event.define(name, config);","};","","// Define outside events for some common native DOM events","Y.Array.each(nativeEvents, function (event) {","    Y.Event.defineOutside(event);","});","","","}, '3.14.1', {\"requires\": [\"event-synthetic\"]});","","}());"]};
}
var __cov_a9nciFiQj0YEROGRBBvZ$A = __coverage__['build/event-outside/event-outside.js'];
__cov_a9nciFiQj0YEROGRBBvZ$A.s['1']++;YUI.add('event-outside',function(Y,NAME){__cov_a9nciFiQj0YEROGRBBvZ$A.f['1']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['2']++;var nativeEvents=['blur','change','click','dblclick','focus','keydown','keypress','keyup','mousedown','mousemove','mouseout','mouseover','mouseup','select','submit'];__cov_a9nciFiQj0YEROGRBBvZ$A.s['3']++;Y.Event.defineOutside=function(event,name){__cov_a9nciFiQj0YEROGRBBvZ$A.f['2']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['4']++;name=(__cov_a9nciFiQj0YEROGRBBvZ$A.b['1'][0]++,name)||(__cov_a9nciFiQj0YEROGRBBvZ$A.b['1'][1]++,event+'outside');__cov_a9nciFiQj0YEROGRBBvZ$A.s['5']++;var config={on:function(node,sub,notifier){__cov_a9nciFiQj0YEROGRBBvZ$A.f['3']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['6']++;sub.handle=Y.one('doc').on(event,function(e){__cov_a9nciFiQj0YEROGRBBvZ$A.f['4']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['7']++;if(this.isOutside(node,e.target)){__cov_a9nciFiQj0YEROGRBBvZ$A.b['2'][0]++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['8']++;e.currentTarget=node;__cov_a9nciFiQj0YEROGRBBvZ$A.s['9']++;notifier.fire(e);}else{__cov_a9nciFiQj0YEROGRBBvZ$A.b['2'][1]++;}},this);},detach:function(node,sub,notifier){__cov_a9nciFiQj0YEROGRBBvZ$A.f['5']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['10']++;sub.handle.detach();},delegate:function(node,sub,notifier,filter){__cov_a9nciFiQj0YEROGRBBvZ$A.f['6']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['11']++;sub.handle=Y.one('doc').delegate(event,function(e){__cov_a9nciFiQj0YEROGRBBvZ$A.f['7']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['12']++;if(this.isOutside(node,e.target)){__cov_a9nciFiQj0YEROGRBBvZ$A.b['3'][0]++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['13']++;notifier.fire(e);}else{__cov_a9nciFiQj0YEROGRBBvZ$A.b['3'][1]++;}},filter,this);},isOutside:function(node,target){__cov_a9nciFiQj0YEROGRBBvZ$A.f['8']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['14']++;return(__cov_a9nciFiQj0YEROGRBBvZ$A.b['4'][0]++,target!==node)&&(__cov_a9nciFiQj0YEROGRBBvZ$A.b['4'][1]++,!target.ancestor(function(p){__cov_a9nciFiQj0YEROGRBBvZ$A.f['9']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['15']++;return p===node;}));}};__cov_a9nciFiQj0YEROGRBBvZ$A.s['16']++;config.detachDelegate=config.detach;__cov_a9nciFiQj0YEROGRBBvZ$A.s['17']++;Y.Event.define(name,config);};__cov_a9nciFiQj0YEROGRBBvZ$A.s['18']++;Y.Array.each(nativeEvents,function(event){__cov_a9nciFiQj0YEROGRBBvZ$A.f['10']++;__cov_a9nciFiQj0YEROGRBBvZ$A.s['19']++;Y.Event.defineOutside(event);});},'3.14.1',{'requires':['event-synthetic']});
