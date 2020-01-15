package com.drkiettran.zookeeper;

import spock.lang.Specification

class GroupSpecification extends Specification {
	def "list znodes"() {
		given:
			Group group = Group.getInstance()
		when:
			group.list()
		then:
			1 == 2
	}
}