package com.example.nosh.fragments.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.startup.AppInitializer;

import com.example.nosh.R;
import com.example.nosh.controller.RecipeController;
import com.example.nosh.database.Initializer.DBControllerFactoryInitializer;
import com.example.nosh.database.Initializer.FirebaseStorageControllerInitializer;
import com.example.nosh.database.controller.DBControllerFactory;
import com.example.nosh.database.controller.FirebaseStorageController;
import com.example.nosh.database.controller.RecipeDBController;
import com.example.nosh.entity.Ingredient;
import com.example.nosh.entity.Recipe;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;


public class RecipesFragment extends Fragment implements Observer {
    //Initalize some needed variables
    private ImageButton addBtn;
    private RecipeAdapter adapter;
    private RecipeController controller;
    private RecipesFragmentListener listener;
    private HashMap<String, StorageReference> recipeImagesRemote;
    private ArrayList<Recipe> recipes;

    /**
     * A event listener class. This class listen all events such as click
     */
    private class RecipesFragmentListener implements
            View.OnClickListener, RecipeAdapter.RecyclerViewListener,
            FragmentResultListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == addBtn.getId()) {
                openAddRecipeDialog();
            }
        }

        @Override
        public void onEditClick(int pos) {
            Recipe recipe = recipes.get(pos);
//            openEditIngredientDialog(ingredient);
        }

        @Override
        public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
            if (requestKey.equals("add_recipe")) {
                ArrayList<Ingredient> ing = new ArrayList<>();
               controller.add(result.getDouble("prep"),
                       result.getInt("servings") ,
                        result.getString("category"),
                        result.getString("comments"),
                        result.getString("photo"),
                        result.getString("name"),
                        ing
                        );
            }
        }

        private void openAddRecipeDialog() {
            AddRecipeDialog addRecipeDialog = AddRecipeDialog.newInstance();
            addRecipeDialog.show(getParentFragmentManager(), "ADD_RECIPE");
        }
    }

    public RecipesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RecipesFragment.
     */
    public static RecipesFragment newInstance() {
        return new RecipesFragment();
    }

    /**
     * on creation of fragment we run this class to inialtize
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DBControllerFactory factory = AppInitializer
                .getInstance(requireContext())
                .initializeComponent(DBControllerFactoryInitializer.class);

        FirebaseStorageController storageController = AppInitializer
                .getInstance(requireContext())
                .initializeComponent(FirebaseStorageControllerInitializer.class);

        controller = new RecipeController(
                requireContext(),
                factory.createAccessController(RecipeDBController.class.getSimpleName()),
                storageController,
                this);

        listener = new RecipesFragmentListener();

        recipes = controller.retrieve();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_recipes, container, false);


        RecyclerView recyclerView = v.findViewById(R.id.recipe_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        adapter = new RecipeAdapter(recipes, getContext(), recipeImagesRemote, listener);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        addBtn = v.findViewById(R.id.add_recipe_btn);

        addBtn.setOnClickListener(listener);

        requireActivity()
                .getSupportFragmentManager()
                .setFragmentResultListener(
                        "add_recipe",
                        getViewLifecycleOwner(),
                        listener);

        return v;
    }

    /**
     * Receive notification from Recipe Repository that there are new changes in
     * data / entity objects. Retrieve the latest copy of the data
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        recipes = controller.retrieve();
        recipeImagesRemote = controller.getRecipeImagesRemote();

        adapter.update(recipes, recipeImagesRemote);
        adapter.notifyItemRangeChanged(0, recipes.size());
    }
}