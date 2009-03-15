import groovy.sql.Sql

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/brits", "brits",
                      "brits", "com.mysql.jdbc.Driver")

new File("/Users/cdupuis/Development/Java/work/spring-ide/required-bundles").eachFile({c-> 
		int i = c.name.lastIndexOf('-')
	
		boolean isSpring = c.name.startsWith("org.springframework")
		if (i > -1) {
			
			String version = c.name.substring(i+1, c.name.length() - 4)
			String bsn = c.name.substring(0,i)
			
			sql.eachRow("select *, concat_ws('.',major, minor,micro,qualifier) as version from t_artefact where symbolic_name = ${bsn} and (concat_ws('.',major, minor,micro,qualifier) = ${version} or concat_ws('.',major, minor,micro) = ${version}) and artefact_type = 'BUNDLE'") {
			    if (isSpring) {
					println "plugin@" + bsn + "=GET,http://repository.springsource.com/ivy/bundles/release/${it.organisation}/${it.module_name}/${version}/${it.module_name}-${version}.jar"
				}
				else {
					println "plugin@" + bsn + "=GET,http://repository.springsource.com/ivy/bundles/external/${it.organisation}/${it.module_name}/${version}/${it.module_name}-${version}.jar" 

				}
			}
		}
	}
)