package TDS.Proctor.performance.dao.impl;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.DbComparator;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Proctor.performance.dao.ItemBankDao;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.LegacySqlConnection;

import java.sql.SQLException;
import java.util.Iterator;

@Repository
public class ItemBankDaoImpl extends AbstractDLL implements ItemBankDao {
    private static final Logger logger = LoggerFactory.getLogger(ItemBankDaoImpl.class);

    @Autowired
    private LegacySqlConnection legacySqlConnection;

    /**
     * This method differs from SQL function because it returns comma separated,
     * single-quoted list of codes only(but not labels) selected VS SQL function
     * returning a table with code and label as columns It is suitable for the
     * manner in which they method is used and it decreases number of temporary
     * tables created.
     *
     * Replaces CommonDll.ITEMBANK_TestLanguages_FN and adds caching to it
     * @param testKey
     * @return
     * @throws ReturnStatusException
     */
    @Cacheable(CacheType.MediumTerm)
    public String getTestLanguages(String testKey) throws ReturnStatusException {

        String codeStr = "";
        Boolean segmented = false;
        String algorithm = null;

        try (SQLConnection connection = legacySqlConnection.get()) {
            final String SQL_QUERY1 = "select IsSegmented as segmented, selectionalgorithm as algorithm from ${ItemBankDB}.tblsetofadminsubjects where _KEy = ${testkey};";
            String query1 = fixDataBaseNames (SQL_QUERY1);
            SqlParametersMaps parameters1 = (new SqlParametersMaps ()).put ("testkey", testKey);
            SingleDataResultSet result = executeStatement (connection, query1, parameters1, false).getResultSets ().next ();
            DbResultRecord record = result.getCount () > 0 ? result.getRecords ().next () : null;
            if (record != null) {
                segmented = record.<Boolean> get ("segmented");
                algorithm = record.<String> get ("algorithm");
            }
            if (DbComparator.isEqual (segmented, false)) {
                if (DbComparator.isEqual ("fixedform", algorithm)) {
                    final String SQL_QUERY2 = " select distinct propvalue as code, propdescription as label from ${ItemBankDB}.tblitemprops P, ${ItemBankDB}.testform F where P._fk_AdminSubject = ${testKey} and propname = ${language}"
                            + " and F._fk_AdminSubject = ${testkey} and F.Language = P.propvalue and P.isactive = 1";
                    String query2 = fixDataBaseNames (SQL_QUERY2);
                    SqlParametersMaps parameters2 = (new SqlParametersMaps ()).put ("testkey", testKey).put ("language", "language");
                    result = executeStatement (connection, query2, parameters2, false).getResultSets ().next ();
                    Iterator<DbResultRecord> records = result.getRecords ();
                    while (records.hasNext ()) {
                        record = records.next ();
                        String code = record.<String> get ("code");
                        if (code != null && code.isEmpty () == false) {
                            if (codeStr.isEmpty ())
                                codeStr = String.format ("'%s'", code);
                            else
                                codeStr += String.format (",'%s'", code);
                        }
                    }
                } else {
                    final String SQL_QUERY3 = "select distinct propvalue as code, propdescription as label from  ${ItemBankDB}.tblitemprops P where P._fk_AdminSubject = ${testKey} and propname = ${language} and isactive = 1";
                    String query3 = fixDataBaseNames (SQL_QUERY3);
                    SqlParametersMaps parameters3 = (new SqlParametersMaps ()).put ("testkey", testKey).put ("Language", "Language");
                    result = executeStatement (connection, query3, parameters3, false).getResultSets ().next ();
                    Iterator<DbResultRecord> records = result.getRecords ();
                    while (records.hasNext ()) {
                        record = records.next ();
                        String code = record.<String> get ("code");
                        if (code != null && code.isEmpty () == false) {
                            if (codeStr.isEmpty ())
                                codeStr = String.format ("'%s'", code);
                            else
                                codeStr += String.format (",'%s'", code);
                        }
                    }
                }
            } else {
                final String SQL_QUERY4 = "select distinct propvalue as code, propdescription as label from ${ItemBankDB}.tblsetofadminitems A, ${ItemBankDB}.tblitemprops P, ${ItemBankDB}.tblsetofadminsubjects S where S.VirtualTest = ${testkey} "
                        + "and A._fk_AdminSubject = S._Key and A._fk_AdminSubject = P._fk_AdminSubject and A._fk_Item = P._fk_Item and propname = ${language} and P.isactive = 1";
                String query4 = fixDataBaseNames (SQL_QUERY4);
                SqlParametersMaps parameters4 = (new SqlParametersMaps ()).put ("testkey", testKey).put ("Language", "Language");
                result = executeStatement (connection, query4, parameters4, false).getResultSets ().next ();
                Iterator<DbResultRecord> records = result.getRecords ();
                while (records.hasNext ()) {
                    record = records.next ();
                    String code = record.<String> get ("code");
                    if (code != null && code.isEmpty () == false) {
                        if (codeStr.isEmpty ())
                            codeStr = String.format ("'%s'", code);
                        else
                            codeStr += String.format (",'%s'", code);
                    }
                }
            }
            return codeStr;
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new ReturnStatusException(e);
        }


    }
}
