# k8055
A scala / Play Framework restful interface for a K8055 board

# Home page
<p>GET     /                           returns JSON representation of all configured devices and their status</p>
<p>GET     /devices                    Same as /</p>
<p>GET     /device/:id                 returns JSON representation of a specified, configured device and its status</p>
<p>POST    /device                     Accepts a JSON representation of a new device, to add the configuration</p>
<p>PUT     /device                     Accepts a JSON representation of an existng device, to update the configuration</p>
<p>PUT     /deviceState                Accepts a JSON representation of an existng device, to update the transient data</p>
<p>PUT     /deviceStateDelta           Accepts a JSON representation of an existng device, to relatively update the transient data </p>
<p>DELETE  /device/:id                 Deletes a specified, configured device</p>
