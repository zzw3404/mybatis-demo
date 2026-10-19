package study.trade;
import java.sql.*;
/** Additive, idempotent upgrade. Existing inventory and trade records are preserved. */
final class TradeSchema {
 static void upgrade(Connection c)throws SQLException {
  try(var s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS lab_trade_brand(id BIGINT PRIMARY KEY,brand_name VARCHAR(60) NOT NULL UNIQUE,website VARCHAR(160))");}
  for(String[] spec:new String[][]{{"brand_id","BIGINT"},{"version","INT NOT NULL DEFAULT 0"},{"create_time","TIMESTAMP DEFAULT CURRENT_TIMESTAMP"},{"update_time","TIMESTAMP DEFAULT CURRENT_TIMESTAMP"}})
   if(!hasColumn(c,spec[0]))try(var s=c.createStatement()){s.executeUpdate("ALTER TABLE lab_trade_product ADD COLUMN "+spec[0]+" "+spec[1]);}
  String[] brands={"星河","远山","流光","云帆","晨音"};
  for(int i=0;i<brands.length;i++){
   try(var s=c.prepareStatement("INSERT INTO lab_trade_brand(id,brand_name,website) SELECT ?,?,? WHERE NOT EXISTS(SELECT 1 FROM lab_trade_brand WHERE brand_name=?) AND NOT EXISTS(SELECT 1 FROM lab_trade_brand WHERE id=?)")){
    s.setInt(1,i+1);s.setString(2,brands[i]);s.setString(3,"https://example.com/brand/"+(i+1));s.setString(4,brands[i]);s.setInt(5,i+1);s.executeUpdate();
   }
  }
  try(var s=c.createStatement()){s.executeUpdate("UPDATE lab_trade_product SET brand_id=(SELECT b.id FROM lab_trade_brand b WHERE b.brand_name=lab_trade_product.brand) WHERE brand_id IS NULL AND EXISTS(SELECT 1 FROM lab_trade_brand b WHERE b.brand_name=lab_trade_product.brand)");}
  if(!hasIndex(c,"idx_trade_brand"))try(var s=c.createStatement()){s.executeUpdate("CREATE INDEX idx_trade_brand ON lab_trade_product(brand_id)");}
  if(!hasIndex(c,"idx_trade_product_name"))try(var s=c.createStatement()){s.executeUpdate("CREATE INDEX idx_trade_product_name ON lab_trade_product(product_name)");}
  boolean fk=false;try(var keys=c.getMetaData().getImportedKeys(c.getCatalog(),null,table(c))){while(keys.next())if("brand_id".equalsIgnoreCase(keys.getString("FKCOLUMN_NAME")))fk=true;}
  if(!fk)try(var s=c.createStatement()){s.executeUpdate("ALTER TABLE lab_trade_product ADD CONSTRAINT fk_trade_product_brand FOREIGN KEY(brand_id) REFERENCES lab_trade_brand(id)");}
  c.commit();
 }
 private static String table(Connection c)throws SQLException{return c.getMetaData().storesUpperCaseIdentifiers()?"LAB_TRADE_PRODUCT":"lab_trade_product";}
 private static boolean hasColumn(Connection c,String name)throws SQLException{try(var r=c.getMetaData().getColumns(c.getCatalog(),null,table(c),null)){while(r.next())if(name.equalsIgnoreCase(r.getString("COLUMN_NAME")))return true;}return false;}
 private static boolean hasIndex(Connection c,String name)throws SQLException{try(var r=c.getMetaData().getIndexInfo(c.getCatalog(),null,table(c),false,false)){while(r.next())if(name.equalsIgnoreCase(r.getString("INDEX_NAME")))return true;}return false;}
}
