package TDS.Proctor.performance.dao;

import TDS.Shared.Exceptions.ReturnStatusException;

public interface ItemBankDao {
    String getTestLanguages(String testKey) throws ReturnStatusException;
}
