# k8055
A scala / Play Framework restful interface for a K8055 board

# Home page
GET     /                           returns JSON representation of all configured devices and their status
GET     /devices                    Same as /
GET     /device/:id                 returns JSON representation of a specified, configured device and its status
POST    /device                     Accepts a JSON representation of a new device, to add the configuration
PUT     /device                     Accepts a JSON representation of an existng device, to update the configuration
PUT     /deviceState                Accepts a JSON representation of an existng device, to update the transient data
PUT     /deviceStateDelta           Accepts a JSON representation of an existng device, to relatively update the transient data 
DELETE  /device/:id                 Deletes a specified, configured device
