package grails.plugin.gson.serialization

import com.google.gson.*
import grails.persistence.Entity
import grails.plugin.gson.adapters.*
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.IgnoreRest

@Mock([Pirate, Ship])
class BasicCollectionSpec extends Specification {

	Gson gson

	void setupSpec() {
		defineBeans {
			proxyHandler DefaultEntityProxyHandler
			domainSerializer GrailsDomainSerializer, ref('grailsApplication'), ref('proxyHandler')
			domainDeserializer GrailsDomainDeserializer, ref('grailsApplication')
			gsonBuilder(GsonBuilderFactory) {
				pluginManager = ref('pluginManager')
			}
		}
	}

	void setup() {
		def gsonBuilder = applicationContext.getBean('gsonBuilder', GsonBuilder)
		gsonBuilder.setDateFormat('yyyy-MM-dd')
		gson = gsonBuilder.create()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [
			name: 'Blackbeard',
			commands: ["Queen Anne's Revenge", 'Adventure']
		]
		def json = gson.toJson(data)

		when:
		def pirate = gson.fromJson(json, Pirate)

		then:
		pirate.name == data.name
		pirate.commands == data.commands
	}

	void 'can deserialize an existing instance'() {
		given:
		def pirate1 = new Pirate(name: 'Blackbeard', commands: ["Queen Anne's Revenge"]).save(failOnError: true)

		and:
		def data = [
			id: pirate1.id,
			commands: ["Queen Anne's Revenge", 'Adventure']
		]
		def json = gson.toJson(data)

		when:
		def pirate2 = gson.fromJson(json, Pirate)

		then:
		pirate2.id == pirate1.id
		pirate2.name == pirate1.name
		pirate2.commands == data.commands
	}

	void 'can serialize an instance'() {
		given:
		def pirate = new Pirate(name: 'Blackbeard', commands: ["Queen Anne's Revenge", 'Adventure']).save(failOnError: true)

		expect:
		def json = gson.toJsonTree(pirate)
		json.commands.get(0).asString == pirate.commands[0]
		json.commands.get(1).asString == pirate.commands[1]
	}

	void 'can deserialize a new instance with date list'() {
		given:
		def data = [
			name: "Queen Anne’s Revenge",
			maintenanceDates: [Date.parse('yyyy-MM-dd', '1717-03-24'), Date.parse('yyyy-MM-dd', '1717-07-08')]
		]
		def json = gson.toJson(data)

		when:
		def ship = gson.fromJson(json, Ship)

		then:
		ship.name == data.name
		ship.maintenanceDates == data.maintenanceDates
	}
}

@Entity
class Pirate {
	String name
	List<String> commands

	static hasMany = [commands: String]
}

@Entity
class Ship {
	String name
	List<Date> maintenanceDates

	static hasMany = [maintenanceDates: Date]
}
