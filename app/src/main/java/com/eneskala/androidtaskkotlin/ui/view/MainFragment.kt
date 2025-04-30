package com.eneskala.androidtaskkotlin.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.eneskala.androidtaskkotlin.R
import com.eneskala.androidtaskkotlin.data.util.Status
import com.eneskala.androidtaskkotlin.databinding.FragmentMainBinding
import com.eneskala.androidtaskkotlin.ui.adapter.TaskAdapter
import com.eneskala.androidtaskkotlin.ui.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var scanResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(requireActivity()).get(TaskViewModel::class.java)

        viewModel.refresh()

        //swipe
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.progressBar.visibility = View.GONE
        }

        // When I click on the searchview, I open the background.
        binding.searchView.setOnSearchClickListener {
            // full width
            binding.searchView.layoutParams =
                binding.searchView.layoutParams.apply { width = ViewGroup.LayoutParams.MATCH_PARENT }
            // background
            binding.searchView.setBackgroundResource(R.drawable.searchview_background)
        }

        // When I click on the icon again, I remove the background and shrink it again.
        binding.searchView.setOnCloseListener {
            // background
            binding.searchView.background = null
            false  // close
        }

        // SearchView Listener
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(text: String): Boolean {
                viewModel.setQuery(text)
                return true
            }
            override fun onQueryTextSubmit(query: String) = true
        })

        // permission for camera
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if(granted) {
                startQrScanner()
            }else {
                Toast.makeText(requireContext(), "Camera permission was denied. You can grant permission again from settings.", Toast.LENGTH_SHORT).show()
            }
        }
        // I am getting qr result
        scanResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            data?.let {
                val qrText = data.getStringExtra("SCAN_RESULT") ?: IntentIntegrator.parseActivityResult(result.resultCode,data).contents
                qrText?.let {
                    viewModel.setQuery(it)
                }
            }
        }

        // when qr code is clicked
        binding.btnScan.setOnClickListener {
            checkCameraPermission()
        }



        subscribeTheObservers()

        return binding.root
    }

    //observe
    private fun subscribeTheObservers() {
        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer { res ->
            when(res.status) {
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE

                }
                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                    Log.d("error","${res.message}")
                }
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })

        viewModel.tasks.observe(viewLifecycleOwner, Observer { list ->
            val adapter = TaskAdapter(list)
            binding.mainRecyclerView.adapter = adapter
            binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        })

    }

    // code for camerapermission
    private fun checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
                Snackbar.make(requireView(), "Kamera izni gerekiyor.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin ver") {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }.show()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            startQrScanner()
        }
    }


    private fun startQrScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Lütfen QR kodu tarayın") // info for users
        integrator.setBeepEnabled(true) // success voice
        integrator.setBarcodeImageEnabled(false)
        integrator.setCameraId(0) // rear camera
        scanResultLauncher.launch(integrator.createScanIntent()) // start scanner
    }



}