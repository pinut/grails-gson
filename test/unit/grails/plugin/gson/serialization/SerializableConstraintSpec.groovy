package grails.plugin.gson.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import grails.persistence.Entity
import grails.plugin.gson.adapters.GrailsDomainDeserializer
import grails.plugin.gson.adapters.GrailsDomainSerializer
import grails.plugin.gson.spring.GsonBuilderFactory
import grails.plugin.gson.support.proxy.DefaultEntityProxyHandler
import grails.test.mixin.Mock
import spock.lang.Issue
import spock.lang.Specification

@Mock(ActionHero)
class SerializableConstraintSpec extends Specification {

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
		gson = gsonBuilder.create()
	}

	void 'can deserialize a new instance'() {
		given:
		def data = [name: 'Iron Man', realName: 'Tony Stark']
		def json = gson.toJson(data)

		when:
		def hero = gson.fromJson(json, ActionHero)

		then:
		hero.name == data.name
		hero.realName == data.realName
	}

	void 'can deserialize an existing instance'() {
		given:
		def hero1 = new ActionHero(name: 'Iron Man', realName: 'James Rhodes').save(failOnError: true)

		and:
		def data = [id: hero1.id, realName: 'Tony Stark']
		def json = gson.toJson(data)

		when:
		def hero2 = gson.fromJson(json, ActionHero)

		then:
		hero2.name == hero1.name
		hero2.realName == data.realName
	}

	@Issue('https://github.com/robfletcher/grails-gson/issues/22')
	void 'can deserialize a new instance with extra json fields'() {
		given:
		def data = [name: 'Iron Man', realName: 'Tony Stark', superPower: 'Iron suit']
		def json = gson.toJson(data)

		when:
		def hero = gson.fromJson(json, ActionHero)

		then:
		hero.name == data.name
		hero.realName == data.realName
	}

	void 'can serialize an instance without non-serializable fields'() {
		given:
		def hero = new ActionHero(name: 'Iron Man', realName: 'Tony Stark').save(failOnError: true)

		expect:
		def json = gson.toJsonTree(hero)
		json.entrySet().size() == 2
		json.id.asLong == hero.id
		json.name.asString == hero.name
		!json.has('realName')
	}

}

@Entity
class ActionHero {
	String name
	String realName

	static constraints = {
		realName serializable: false
	}
}