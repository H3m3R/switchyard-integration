SwitchYard Integration
======================

1. HornetQ (XML) to ActiveMQ (JSON) and File (CSV)

2. ActiveMQ (JSON) to HornetQ (XML)

JBoss EAP 6.1 setup
-------------------
1. Start standalone JBoss EAP 6.1 with specific configuration:

        cp setup/standalone-switchyard-integration.xml ${JBOSS_HOME}/standalone/configuration

        ${JBOSS_HOME}/bin/standalone.sh -c standalone-switchyard-integration.xml

2. Add management user using add-user.sh with username=admin, password=adminp.1, Realm=ManagementRealm

        ${JBOSS_HOME}/bin/add-user.sh

3. Deploy activemq-ra.rar

        cp setup/activemq-ra.rar ${JBOSS_HOME}/standalone/deployments


Tests
=========================

1. Create own developer.properties and set output file directory

        cp src/test/resources/developer.properties_template src/test/resources/developer.properties

2. Build and package:

        mvn clean package

3. For standalone unit tests run:

        mvn test -DskipTests=false -Dgroups=unit

4. For Arquillian integration tests run:

        mvn test -DskipTests=false -Dgroups=arquillian,integration