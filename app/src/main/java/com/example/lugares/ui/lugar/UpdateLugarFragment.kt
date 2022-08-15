package com.example.lugares.ui.lugar

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.lugares.databinding.FragmentUpdateLugarBinding
import com.example.lugares.model.Lugar
import com.example.lugares.viewmodel.LugarViewModel
import com.example.lugares.R

class UpdateLugarFragment : Fragment() {
    private val args by navArgs<UpdateLugarFragmentArgs>()

    private var _binding: FragmentUpdateLugarBinding? = null
    private val binding get() = _binding!!
    private lateinit var lugarViewModel: LugarViewModel

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateLugarBinding.inflate(inflater, container, false)
        lugarViewModel = ViewModelProvider(this)[LugarViewModel::class.java]

        binding.etNombre.setText(args.lugar.nombre)
        binding.etCorreo.setText(args.lugar.correo)
        binding.etTelefono.setText(args.lugar.telefono)
        binding.etWeb.setText(args.lugar.sitioWeb)
        binding.tvLongitud.text = args.lugar.longitud.toString()
        binding.tvLatitud.text = args.lugar.latitud.toString()
        binding.tvAltura.text = args.lugar.altura.toString()

        binding.btActualizar.setOnClickListener { updateLugar()}
        binding.btEmail.setOnClickListener { escribirCorreo() }
        binding.btPhone.setOnClickListener { realizarLlamada() }
        binding.btWhatsapp.setOnClickListener {  enviarWhatsApp() }
        binding.btWeb.setOnClickListener { verWeb() }
        binding.btLocation.setOnClickListener { verMapa() }

        if(args.lugar.rutaAudio?.isNotEmpty() == true){
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(args.lugar.rutaAudio)
            mediaPlayer.prepare()
            binding.btPlay.isEnabled = true
        }else{
            binding.btPlay.isEnabled = false
        }

        if(args.lugar.rutaImagen?.isNotEmpty() == true){
            Glide.with(requireContext())
                .load(args.lugar.rutaImagen)
                .fitCenter()
                .into(binding.imagen)
        }

        binding.btPlay.setOnClickListener { mediaPlayer.start() }

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.menu_delete) {
            deleteLugar()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun realizarLlamada() {
        val recurso = binding.etTelefono.text.toString()
        if (recurso.isNotEmpty()) {
            val accion = Intent(Intent.ACTION_CALL)
            accion.data = Uri.parse("tel:$recurso")
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
                requireActivity()
                    .requestPermissions(arrayOf(Manifest.permission.CALL_PHONE),105)
            } else {
                requireActivity().startActivity(accion)
            }
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarWhatsApp(){
        val telefono = binding.etTelefono.text.toString()
        if (telefono.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = "whatsapp://send?phone=506$telefono&text=" + getString(R.string.msg_saludos)
            intent.setPackage("com.whatsapp")
            intent.data = Uri.parse(uri)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun escribirCorreo() {
        val recurso = binding.etCorreo.text.toString()
        if (recurso.isNotEmpty()) {
            val accion = Intent(Intent.ACTION_SEND)
            accion.type="message/rfc822"
            accion.putExtra(Intent.EXTRA_EMAIL,arrayOf(recurso))
            accion.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.msg_saludos)+" "+binding.etNombre.text)
            accion.putExtra(Intent.EXTRA_TEXT,getString(R.string.msg_mensaje_correo))
            startActivity(accion)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun verWeb(){
        val sitioWeb = binding.etWeb.text.toString()
        if (sitioWeb.isNotEmpty()) {
            val uri = Uri.parse("http://$sitioWeb")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun verMapa(){
        val latitud = binding.tvLatitud.text.toString().toDouble()
        val longitud = binding.tvLongitud.text.toString().toDouble()

        if (latitud.isFinite() && longitud.isFinite()) {
            val location = Uri.parse("geo:$latitud,$longitud?z=18")
            val intent = Intent(Intent.ACTION_VIEW, location)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLugar() {
        val nombre = binding.etNombre.text.toString()
        val correo = binding.etCorreo.text.toString()
        val telefono = binding.etTelefono.text.toString()
        val web = binding.etWeb.text.toString()

        if(validos(nombre, correo, telefono, web)){
            val lugar= Lugar(
                args.lugar.id,
                nombre,
                correo,
                telefono,
                web,
                args.lugar.longitud,
                args.lugar.latitud,
                args.lugar.altura,
                args.lugar.rutaAudio,
                args.lugar.rutaImagen)
            lugarViewModel.updateLugar(lugar)
            Toast.makeText(requireContext(),getString(R.string.msg_lugar_update), Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        }else{
            Toast.makeText(requireContext(),getString(R.string.msg_data),Toast.LENGTH_SHORT).show()
        }
    }

    private fun validos(nombre: String, correo: String, telefono: String, web: String) : Boolean {
        return !(nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || web.isEmpty())
    }

    private fun deleteLugar() {
        val pantalla= AlertDialog.Builder(requireContext())

        pantalla.setTitle(R.string.delete)
        pantalla.setMessage(getString(R.string.seguroBorrar)+" ${args.lugar.nombre}?")

        pantalla.setPositiveButton(getString(R.string.si)) { _,_ ->
            lugarViewModel.deleteLugar(args.lugar)
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        }

        pantalla.setNegativeButton(getString(R.string.no)) { _,_ -> }
        pantalla.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}