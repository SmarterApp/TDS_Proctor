alter table `client_fieldtestpriority`
	add constraint `fk_ft_testeeatt`
	foreign key(`clientname`, `tds_id`)
	references `client_testeeattribute`(`clientname`, `tds_id`)
	on delete cascade 
	on update cascade ;

alter table `client_messagetranslation`
	add constraint `fk_clientmsgtranslation`
	foreign key(`_fk_coremessageobject`)
	references `tds_coremessageobject`(`_key`)
	on delete cascade 
	on update no action ;

alter table `client_tds_rtsattributevalues`
	add constraint `fk_client_tds_rtsattributevalues_client_tds_rtsattribute`
	foreign key(`clientname`, `fieldname`, `_efk_entitytype`, `type`)
	references `client_tds_rtsattribute`(`clientname`, `fieldname`, `_efk_entitytype`, `type`)
	on delete no action 
	on update no action ;

alter table `client_testtool`
	add constraint `fk_clienttool_tooltype`
	foreign key(`clientname`, `context`, `contexttype`, `type`)
	references `client_testtooltype`(`clientname`, `context`, `contexttype`, `toolname`)
	on delete cascade 
	on update cascade ;

alter table `client_testwindow`
	add constraint `fk_timewindow`
	foreign key(`clientname`, `windowid`)
	references `client_timewindow`(`clientname`, `windowid`)
	on delete no action 
	on update cascade ;

alter table `geo_clientapplication`
	add constraint `fk_geoapp_db`
	foreign key(`_fk_geo_database`)
	references `geo_database`(`_key`)
	on delete no action 
	on update no action ;

alter table `system_applicationsettings`
	add constraint `tds_applicationsettings_client_applicationsettings`
	foreign key(`_fk_tds_applicationsettings`)
	references `tds_applicationsettings`(`_key`)
	on delete cascade 
	on update no action ;

alter table `tds_coremessageuser`
	add constraint `fk_msguser_msgobj`
	foreign key(`_fk_coremessageobject`)
	references `tds_coremessageobject`(`_key`)
	on delete no action 
	on update no action ;

alter table `tds_fieldtestpriority`
	add constraint `fk_tds_testeeatt`
	foreign key(`tds_id`)
	references `tds_testeeattribute`(`tds_id`)
	on delete no action 
	on update no action ;
