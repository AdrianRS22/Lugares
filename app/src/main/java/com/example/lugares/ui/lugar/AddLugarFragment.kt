package com.example.lugares.ui.lugar

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.lugares.R
import com.example.lugares.databinding.FragmentAddLugarBinding
import com.example.lugares.model.Lugar
import com.example.lugares.viewmodel.LugarViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddLugarFragment : Fragment() {

    private var _binding: FragmentAddLugarBinding? = null
    private val binding get() = _binding!!

    private lateinit var lugarViewModel: LugarViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLugarBinding.inflate(inflater, container, false)

        lugarViewModel = ViewModelProvider(this).get(LugarViewModel::class.java)

        binding.btAgregar.setOnClickListener { addLugar() }

        ubicaGPS()

        return binding.root
    }

    private fun ubicaGPS() {
        val ubicacion: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),105)
        } else {
            ubicacion.lastLocation.addOnSuccessListener { location: Location? ->
                if (location!=null) {
                    binding.tvLatitud.text ="${location.latitude}"
                    binding.tvLongitud.text ="${location.longitude}"
                    binding.tvAltura.text ="${location.altitude}"
                } else {
                    binding.tvLatitud.text ="0.00"
                    binding.tvLongitud.text ="0.00"
                    binding.tvAltura.text ="0.00"
                }
            }
        }
    }

    private fun addLugar(){
        var nombre = binding.etNombre.text.toString()
        var correo = binding.etCorreo.text.toString()
        var telefono = binding.etTelefono.text.toString()
        var web = binding.etWeb.text.toString()
        val longitud = binding.tvLongitud.text.toString().toDouble()
        val latitud = binding.tvLatitud.text.toString().toDouble()
        val altura = binding.tvAltura.text.toString().toDouble()

        if(validos(nombre, correo, telefono, web)){
            var lugar = Lugar(0,nombre,correo,telefono,web,longitud,latitud,altura,"","")
            lugarViewModel.addLugar(lugar)
            Toast.makeText(requireContext(), getString(R.string.msg_lugar_added), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.msg_lugar_update), Toast.LENGTH_LONG).show()
        }
        findNavController().navigate(R.id.action_addLugarFragment_to_nav_lugar)
    }

    private fun validos(nombre: String, correo: String, telefono: String, web: String) : Boolean {
        return !(nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || web.isEmpty())
    }
}