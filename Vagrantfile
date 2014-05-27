# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  
  ###############################
  # SEARCH VAGRANT FILE
  ###############################
  
  # Every Vagrant virtual environment requires a base box to build off of.
  config.vm.box = "search-vagrant-box"

  # The name of the file to import as a box if the above box isn't found
  # You can find this box in TeamShares/Bahmni Project/VMs
  config.vm.box_url = "Search_Vagrant_Centos.box"

  # The name of the Vagrant VM when it comes up (instead of default)
  config.vm.define "Bahmni-Search-Centos" do |bahmni2|
  end

  config.vm.network :forwarded_port, guest: 8080, host: 8081
  config.vm.network :forwarded_port, guest: 443, host: 8082
  config.vm.network :forwarded_port, guest: 80, host: 8083
  config.vm.network :private_network, ip: "192.168.33.10"
  # config.vm.network :public_network
  config.ssh.username = "vagrant"

  config.vm.synced_folder "..", "/Project", :owner => "vagrant"

  # Virtual Box Parameters
  config.vm.provider "virtualbox" do |v|
    v.customize ["modifyvm", :id, "--memory", 3092, "--cpus", 2, "--name", "Bahmni_SearchVM"]
  end
end

