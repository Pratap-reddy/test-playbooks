pipeline {

    agent none

    parameters {
        choice(
            name: 'TOWER_VERSION',
            description: 'Tower version to test',
            choices: ['devel', '3.4.2', '3.3.5', '3.2.9']
        )
        choice(
            name: 'PLATFORM',
            description: 'The OS to install the Tower instance on',
            choices: ['rhel-7.6-x86_64', 'rhel-7.5-x86_64', 'rhel-7.4-x86_64',
                      'rhel-8.0-x86_64', 'ol-7.6-x86_64', 'centos-7.latest-x86_64',
                      'ubuntu-16.04-x86_64', 'ubuntu-14.04-x86_64']
        )
    }

    options {
        timestamps()
    }

    stages {
        stage('Ansible Versions') {
            steps {
                script {
                    def tasks = [:]
                    def ansible_versions = [:]

                    if (params.TOWER_VERSION == 'devel') {
                        ansible_versions = ['devel', 'stable-2.7', 'stable-2.6']
                    } else {
                        ansible_versions = ['devel', 'stable-2.7', 'stable-2.6', 'stable-2.5', 'stable-2.4', 'stable-2.3']
                    }

                    for(int j=0; j<ansible_versions.size(); j++) {
                        def ansible_version = ansible_versions[j]

                        tasks[ansible_version] = {
                            build(
                                job: 'verification-pipeline',
                                parameters: [
                                    string(name: 'TOWER_VERSION', value: params.TOWER_VERSION),
                                    string(name: 'ANSIBLE_VERSION', value: ansible_version),
                                    string(name: 'PLATFORM', value: params.PLATFORM)
                                ]
                            )
                        }
                    }

                    stage ('Ansible Versions') {
                        parallel tasks
                    }
                }
            }
        }
    }
}
