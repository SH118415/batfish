package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IptablesConfiguration implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   Map<String, Table> _tables = new HashMap<String, Table>();

   public void addRule(String tableName, String chainName, IptablesRule rule, int index) {
      addTable(tableName);
      _tables.get(tableName).addRule(chainName, rule, index);
   }

   public void addTable(String tableName) {
      if (!_tables.containsKey(tableName)) {
         _tables.put(tableName, new Table(tableName));
      }
   }
   
   public void addChain(String tableName, String chainName) {
      addTable(tableName);
      _tables.get(tableName).addChain(chainName);
   }

   public void setChainTarget(String tableName, String chainName, String target) {
      addTable(tableName);
      _tables.get(tableName).setChainTarget(chainName, target);
   }
   
}
