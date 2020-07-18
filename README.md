# BlokAtUGent
## Remarks on Code
### Dao Layer
#### DBLocationDao
  * auxiliary method `public static Location createLocation(ResultSet)`  

A public auxiliary method `public static Location createLocation(ResultSet)` will create a Location object from a ResultSet that contains records in which a location was joined with the corresponding LocationDescriptions-records. The auxiliary method loops over the resultset to get all descriptions in the provided languages. This is in the form of 
  
```
// add description of first record to the location object
while (rs.next()) { 
  // add description of the next records to the location object
}
```

This means that you will not be able to use the same ResultSet for other auxiliary object creations, like `DBAccountDao`'s   method `public static User createUser(ResultSet)` <b>after</b> the use of `DBLocationDao`'s auxiliary method `createLocation()`. Use the `createUser()` before `createLocation()` and you'll be fine.
