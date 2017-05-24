# Setting up a VM with Biostudies Submission Tool UI #

## Prerequisites ##
* VirtualBox (>= v5.1.18)
  * vagrant-vbguest plugin is required
````sh  
$ vagrant plugin install vagrant-vbguest
````  
* Vagrant (>= v1.9.1)
* Ansible (>= v2.2.1.0)

## Setting VM up ##
```
$ vagrant up
```

## Configure ##
All build/deploy settings are in the file `./roles/vars.yml`. Feel free customize them as you need.

If you want to use backend service directly from the host machine use 192.168.33.1 ip for it.  


## Checkout/Build/Deploy ##
The following command will do everything: checkout, build and deploy 
```
$ ansible-playbook -i hosts playbook.yml 
```
or to run checkout only:
```
$ ansible-playbook -i hosts playbook.yml --extra-vars "build_on=false deploy_on=false"
```
or to run build only:
```
$ ansible-playbook -i hosts playbook.yml --extra-vars "checkout_on=false deploy_on=false"
```
or to run deploy only:
```
$ ansible-playbook -i hosts playbook.yml --extra-vars "checkout_on=false build_on=false"
```
or to run deploy only (without reinstalling tomcat):
```
$ ansible-playbook -i hosts playbook.yml --extra-vars "checkout_on=false build_on=false install_tomcat_on=false"
```
## Run ##
If there were no errors the following url: `https://192.168.33.16:8443/proxy` should show you submission tool login form.   

