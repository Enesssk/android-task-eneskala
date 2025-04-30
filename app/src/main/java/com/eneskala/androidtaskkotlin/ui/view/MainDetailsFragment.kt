package com.eneskala.androidtaskkotlin.ui.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.eneskala.androidtaskkotlin.R
import com.eneskala.androidtaskkotlin.databinding.FragmentMainDetailsBinding


class MainDetailsFragment : Fragment() {

    private lateinit var binding: FragmentMainDetailsBinding
    private val args: MainDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainDetailsBinding.inflate(inflater,container,false)


        val tasks = args.task // I received the incoming task.

        tasks?.let {
            binding.ColorCodeText.text = "${it.colorCode}" ?: "-"
            binding.businessUnitText.text = "${it.businessUnit}" ?: "-"
            binding.workingTimeText.text = "${it.workingTime}" ?: "-"
            binding.wageTypeText.text = "${it.wageType}" ?: "-"
        }

        binding.backText.setOnClickListener {
            Navigation.findNavController(it).popBackStack()
        }




        return binding.root
    }


}