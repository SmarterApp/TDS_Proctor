/*
YUI 3.14.1 (build 63049cb)
Copyright 2013 Yahoo! Inc. All rights reserved.
Licensed under the BSD License.
http://yuilibrary.com/license/
*/

YUI.add("attribute-core",function(e,t){function b(e,t,n){this._yuievt=null,this._initAttrHost(e,t,n)}e.State=function(){this.data={}},e.State.prototype={add:function(e,t,n){var r=this.data[e];r||(r=this.data[e]={}),r[t]=n},addAll:function(e,t){var n=this.data[e],r;n||(n=this.data[e]={});for(r in t)t.hasOwnProperty(r)&&(n[r]=t[r])},remove:function(e,t){var n=this.data[e];n&&delete n[t]},removeAll:function(t,n){var r;n?e.each(n,function(e,n){this.remove(t,typeof n=="string"?n:e)},this):(r=this.data,t in r&&delete r[t])},get:function(e,t){var n=this.data[e];if(n)return n[t]},getAll:function(e,t){var n=this.data[e],r,i;if(t)i=n;else if(n){i={};for(r in n)n.hasOwnProperty(r)&&(i[r]=n[r])}return i}};var n=e.Object,r=e.Lang,i=".",s="getter",o="setter",u="readOnly",a="writeOnce",f="initOnly",l="validator",c="value",h="valueFn",p="lazyAdd",d="added",v="_bypassProxy",m="initValue",g="lazy",y;b.INVALID_VALUE={},y=b.INVALID_VALUE,b._ATTR_CFG=[o,s,l,c,h,a,u,p,v],b.protectAttrs=function(t){if(t){t=e.merge(t);for(var n in t)t.hasOwnProperty(n)&&(t[n]=e.merge(t[n]))}return t},b.prototype={_initAttrHost:function(t,n,r){this._state=new e.State,this._initAttrs(t,n,r)},addAttr:function(e,t,n){var r=this,i=r._state,s=i.data,o,u,a;t=t||{},p in t&&(n=t[p]),u=i.get(e,d);if(n&&!u)i.data[e]={lazy:t,added:!0};else if(!u||t.isLazyAdd)a=c in t,a&&(o=t.value,t.value=undefined),t.added=!0,t.initializing=!0,s[e]=t,a&&r.set(e,o),t.initializing=!1;return r},attrAdded:function(e){return!!this._state.get(e,d)},get:function(e){return this._getAttr(e)},_isLazyAttr:function(e){return this._state.get(e,g)},_addLazyAttr:function(e,t){var n=this._state;t=t||n.get(e,g),t&&(n.data[e].lazy=undefined,t.isLazyAdd=!0,this.addAttr(e,t))},set:function(e,t,n){return this._setAttr(e,t,n)},_set:function(e,t,n){return this._setAttr(e,t,n,!0)},_setAttr:function(t,r,s,o){var u=!0,a=this._state,l=this._stateProxy,c=this._tCfgs,h,p,d,v,m,g,y;return t.indexOf(i)!==-1&&(d=t,v=t.split(i),t=v.shift()),c&&c[t]&&this._addOutOfOrder(t,c[t]),h=a.data[t]||{},h.lazy&&(h=h.lazy,this._addLazyAttr(t,h)),p=h.value===undefined,l&&t in l&&!h._bypassProxy&&(p=!1),g=h.writeOnce,y=h.initializing,!p&&!o&&(g&&(u=!1),h.readOnly&&(u=!1)),!y&&!o&&g===f&&(u=!1),u&&(p||(m=this.get(t)),v&&(r=n.setValue(e.clone(m),v,r),r===undefined&&(u=!1)),u&&(!this._fireAttrChange||y?this._setAttrVal(t,d,m,r,s,h):this._fireAttrChange(t,d,m,r,s,h))),this},_addOutOfOrder:function(e,t){var n={};n[e]=t,delete this._tCfgs[e],this._addAttrs(n,this._tVals)},_getAttr:function(e){var t=e,r=this._tCfgs,s,o,u,a;return e.indexOf(i)!==-1&&(s=e.split(i),e=s.shift()),r&&r[e]&&this._addOutOfOrder(e,r[e]),a=this._state.data[e]||{},a.lazy&&(a=a.lazy,this._addLazyAttr(e,a)),u=this._getStateVal(e,a),o=a.getter,o&&!o.call&&(o=this[o]),u=o?o.call(this,u,t):u,u=s?n.getValue(u,s):u,u},_getStateVal:function(e,t){var n=this._stateProxy;return t||(t=this._state.getAll(e)||{}),n&&e in n&&!t._bypassProxy?n[e]:t.value},_setStateVal:function(e,t){var n=this._stateProxy;n&&e in n&&!this._state.get(e,v)?n[e]=t:this._state.add(e,c,t)},_setAttrVal:function(e,t,n,i,s,o){var u=this,a=!0,f=o||this._state.data[e]||{},l=f.validator,c=f.setter,h=f.initializing,p=this._getStateVal(e,f),d=t||e,v,g;return l&&(l.call||(l=this[l]),l&&(g=l.call(u,i,d,s),!g&&h&&(i=f.defaultValue,g=!0))),!l||g?(c&&(c.call||(c=this[c]),c&&(v=c.call(u,i,d,s),v===y?h?i=f.defaultValue:a=!1:v!==undefined&&(i=v))),a&&(!t&&i===p&&!r.isObject(i)?a=!1:(m in f||(f.initValue=i),u._setStateVal(e,i)))):a=!1,a},setAttrs:function(e,t){return this._setAttrs(e,t)},_setAttrs:function(e,t){var n;for(n in e)e.hasOwnProperty(n)&&this.set(n,e[n],t);return this},getAttrs:function(e){return this._getAttrs(e)},_getAttrs:function(e){var t={},r,i,s,o=e===!0;if(!e||o)e=n.keys(this._state.data);for(i=0,s=e.length;i<s;i++){r=e[i];if(!o||this._getStateVal(r)!=this._state.get(r,m))t[r]=this.get(r)}return t},addAttrs:function(e,t,n){return e&&(this._tCfgs=e,this._tVals=t?this._normAttrVals(t):null,this._addAttrs(e,this._tVals,n),this._tCfgs=this._tVals=null),this},_addAttrs:function(e,t,n){var r=this._tCfgs,i=this._tVals,s,o,u;for(s in e)e.hasOwnProperty(s)&&(o=e[s],o.defaultValue=o.value,u=this._getAttrInitVal(s,o,i),u!==undefined&&(o.value=u),r[s]&&(r[s]=undefined),this.addAttr(s,o,n))},_protectAttrs:b.protectAttrs,_normAttrVals:function(e){var t,n,r,s,o,u;if(!e)return null;t={};for(u in e)e.hasOwnProperty(u)&&(u.indexOf(i)!==-1?(r=u.split(i),s=r.shift(),n=n||{},o=n[s]=n[s]||[],o[o.length]={path:r,value:e[u]}):t[u]=e[u]);return{simple:t,complex:n}},_getAttrInitVal:function(e,t,r){var i=t.value,s=t.valueFn,o,u=!1,a=t.readOnly,f,l,c,h,p,d,v;!a&&r&&(f=r.simple,f&&f.hasOwnProperty(e)&&(i=f[e],u=!0)),s&&!u&&(s.call||(s=this[s]),s&&(o=s.call(this,e),i=o));if(!a&&r){l=r.complex;if(l&&l.hasOwnProperty(e)&&i!==undefined&&i!==null){v=l[e];for(c=0,h=v.length;c<h;++c)p=v[c].path,d=v[c].value,n.setValue(i,p,d)}}return i},_initAttrs:function(t,n,r){t=t||this.constructor.ATTRS;var i=e.Base,s=e.BaseCore,o=i&&e.instanceOf(this,i),u=!o&&s&&e.instanceOf(this,s);t&&!o&&!u&&this.addAttrs(e.AttributeCore.protectAttrs(t),n,r)}},e.AttributeCore=b},"3.14.1",{requires:["oop"]});
