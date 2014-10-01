create unique index `ix_clientexterns`
	on `client_externs`(`clientname`, `environment`);

create index `ix_clienttestwindow`
	on `client_testwindow`(`clientname`, `testid`);

create unique index `ix_clienttestmode`
	on `client_testmode`(`testkey`, `sessiontype`);

create index `ix_clienttimelimits`
	on `client_timelimits`(`clientname`, `_efk_testid`);

create index `ix_clienttooldependencies`
	on `client_tooldependencies`(`clientname`, `contexttype`, `context`);

create index `ix_clienttooltestid`
	on `client_testtool`(`context`);

create index `ix_coremsgcontext`
	on `tds_coremessageobject`(`context`);

create index `ix_form_testkey`
	on `client_testformproperties`(`testkey`);

create index `ix_geoclientapp`
	on `geo_clientapplication`(`clientname`, `environment`, `appname`, `servicetype`);

create index `ix_msgtrans`
	on `client_messagetranslation`(`_fk_coremessageobject`, `client`, `language`);

create index `ix_testeligibility`
	on `client_testeligibility`(`clientname`, `testid`, `enables`, `rtsname`, `rtsvalue`);

create index `ix_testmode`
	on `client_testmode`(`clientname`, `testid`, `sessiontype`);

create index `ix_testmodekey`
	on `client_testmode`(`testkey`);

create index `ix_testrtsspecs`
	on `client_testrtsspecs`(`clientname`, `testid`);

create index `ix_testgrade_test`
	on `client_testgrades`(`testid`);

create index `ix_testmode_test`
	on `client_testmode`(`testid`);

create index `ix_testprops_test`
	on `client_testproperties`(`testid`);

create index `ix_testwindow_test`
	on `client_testwindow`(`testid`);

create unique index `ix_uniqueappmsg`
	on `tds_coremessageobject`(`ownerapp`, `messageid`, `contexttype`, `context`);
