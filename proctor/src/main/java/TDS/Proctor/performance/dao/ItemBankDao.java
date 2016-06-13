package TDS.Proctor.performance.dao;

import TDS.Shared.Exceptions.ReturnStatusException;

public interface ItemBankDao extends tds.dll.common.performance.dao.ItemBankDao {
    String getTestLanguages(String testKey) throws ReturnStatusException;
}
