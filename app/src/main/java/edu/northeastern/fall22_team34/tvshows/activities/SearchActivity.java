package edu.northeastern.fall22_team34.tvshows.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.northeastern.fall22_team34.R;
import edu.northeastern.fall22_team34.tvshows.adapters.TVShowsAdapter;
import edu.northeastern.fall22_team34.databinding.ActivitySearchBinding;
import edu.northeastern.fall22_team34.tvshows.models.TVShow;
import edu.northeastern.fall22_team34.tvshows.viewmodels.SearchViewModel;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding activitySearchBinding;
    private SearchViewModel viewModel;
    private List<TVShow> tvShows = new ArrayList<>();
    private TVShowsAdapter tvShowsAdapter;
    private int currentPage = 1;
    private int totalAvailablePages = 1;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        //setContentView(R.layout.activity_search);

        doInitialization();
    }

    private void doInitialization() {
        activitySearchBinding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        activitySearchBinding.tvShowRecyclerView.setHasFixedSize(true);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        tvShowsAdapter = new TVShowsAdapter(tvShows);
        activitySearchBinding.tvShowRecyclerView.setAdapter(tvShowsAdapter);

        activitySearchBinding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (timer != null) {
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    currentPage = 1;
                                    totalAvailablePages = 1;
                                    searchTVShow(editable.toString());
                                }
                            });
                        }
                    }, 800);
                } else {
                    tvShows.clear();
                    tvShowsAdapter.notifyDataSetChanged();
                }
            }
        });

        activitySearchBinding.tvShowRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!activitySearchBinding.tvShowRecyclerView.canScrollVertically(1)) {
                    if (!activitySearchBinding.inputSearch.getText().toString().isEmpty()) {
                        if (currentPage < totalAvailablePages) {
                            currentPage += 1;
                            searchTVShow(activitySearchBinding.inputSearch.getText().toString());
                        }
                    }
                }
            }
        });

        activitySearchBinding.inputSearch.requestFocus();
    }

    private void searchTVShow(String query) {
        toggleLoading();
        viewModel.searchTVShow(query, currentPage).observe(this, tvShowsResponse -> {
            toggleLoading();
            if (tvShowsResponse != null) {
                totalAvailablePages = tvShowsResponse.getTotalPages();
                if (tvShowsResponse.getTvShows() != null) {
                    int oldCount = tvShows.size();
                    tvShows.addAll(tvShowsResponse.getTvShows());
                    tvShowsAdapter.notifyItemRangeInserted(oldCount, tvShows.size());
                }
            }
        });
    }

    private void toggleLoading() {
        if (currentPage == 1) {
            if (activitySearchBinding.getIsLoading() != null && activitySearchBinding.getIsLoading()) {
                activitySearchBinding.setIsLoading(false);
            } else {
                activitySearchBinding.setIsLoading(true);
            }
        } else {
            if (activitySearchBinding.getIsLoadingMore() != null && activitySearchBinding.getIsLoadingMore()) {
                activitySearchBinding.setIsLoadingMore(false);
            } else {
                activitySearchBinding.setIsLoadingMore(true);
            }
        }
    }
}