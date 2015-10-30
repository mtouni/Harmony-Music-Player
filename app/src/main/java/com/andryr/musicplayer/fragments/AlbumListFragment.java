package com.andryr.musicplayer.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.andryr.musicplayer.adapters.AlbumListAdapter;
import com.andryr.musicplayer.adapters.BaseAdapter;
import com.andryr.musicplayer.model.Album;
import com.andryr.musicplayer.model.Artist;
import com.andryr.musicplayer.fragments.dialog.AlbumEditorDialog;
import com.andryr.musicplayer.widgets.FastScroller;
import com.andryr.musicplayer.MainActivity;
import com.andryr.musicplayer.model.Playlist;
import com.andryr.musicplayer.utils.Playlists;
import com.andryr.musicplayer.R;
import com.andryr.musicplayer.loaders.AlbumLoader;

import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the
 * {@link AlbumListFragment#newInstance} factory method to create an instance of
 * this fragment.
 */
public class AlbumListFragment extends BaseFragment {

    private static final String PARAM_ARTIST = "artist";
    private static final String PARAM_ARTIST_ALBUM = "artist_album";


    private AlbumListAdapter mAdapter;


    private RecyclerView mRecyclerView;

    private LoaderManager.LoaderCallbacks<List<Album>> mLoaderCallbacks = new LoaderCallbacks<List<Album>>() {

        @Override
        public void onLoaderReset(Loader<List<Album>> loader) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadFinished(Loader<List<Album>> loader, List<Album> data) {
            mAdapter.setData(data);

        }

        @Override
        public Loader<List<Album>> onCreateLoader(int id, Bundle args) {


            return new AlbumLoader(getActivity());
        }
    };


    private AlbumEditorDialog.OnEditionSuccessListener mOnEditionSuccessListener = new AlbumEditorDialog.OnEditionSuccessListener() {
        @Override
        public void onEditionSuccess() {
            ((MainActivity) getActivity()).refresh();
        }
    };
    private BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            Album album = mAdapter.getItem(position);

            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.album_info:
                    Fragment fragment = AlbumFragment.newInstance(album);
                    ((MainActivity) getActivity()).setFragment(fragment);
                    break;
                case R.id.menu_button:
                    showMenu(position, view);
                    break;

            }
        }
    };


    public AlbumListFragment() {
        // Required empty public constructor
    }

    public static AlbumListFragment newInstance(Artist artist) {
        AlbumListFragment fragment = new AlbumListFragment();
        if (artist != null) {
            Bundle args = new Bundle();
            args.putString(PARAM_ARTIST, artist.getName());
            args.putBoolean(PARAM_ARTIST_ALBUM, true);
            fragment.setArguments(args);
        }

        return fragment;
    }

    private void showMenu(final int position, View v) {

        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.album_list_item, popup.getMenu());
        final Album album = mAdapter.getItem(position);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_edit_tags:
                        showEditorDialog(album);
                        return true;
                    case R.id.action_add_to_playlist:
                        showPlaylistPicker(album);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showEditorDialog(Album album) {
        AlbumEditorDialog dialog = AlbumEditorDialog.newInstance(album);
        dialog.setOnEditionSuccessListener(mOnEditionSuccessListener);
        dialog.show(getChildFragmentManager(), "edit_album_tags");
    }

    private void showPlaylistPicker(final Album album) {
        PlaylistPicker picker = PlaylistPicker.newInstance();
        picker.setListener(new PlaylistPicker.OnPlaylistPickedListener() {
            @Override
            public void onPlaylistPicked(Playlist playlist) {
                Playlists.addAlbumToPlaylist(getActivity().getContentResolver(), playlist.getId(), album.getId());
            }
        });
        picker.show(getChildFragmentManager(), "pick_playlist");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     /*   Bundle args = getArguments();
        if (args != null) {
            mArtistAlbum = args.getBoolean(PARAM_ARTIST_ALBUM);
            if (mArtistAlbum) {
                mArtist = args.getString(PARAM_ARTIST);
            }
        }*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_list,
                container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_view);
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Resources res = getActivity().getResources();
        float screenWidth = display.getWidth();
        float itemWidth = res.getDimension(R.dimen.album_grid_item_width);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Math.round(screenWidth / itemWidth)));
        mAdapter = new AlbumListAdapter();
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        FastScroller scroller = (FastScroller) rootView.findViewById(R.id.fastscroller);
        scroller.setSectionIndexer(mAdapter);
        scroller.setRecyclerView(mRecyclerView);

        return rootView;
    }

    @Override
    public void refresh() {
        getLoaderManager().restartLoader(0, null, mLoaderCallbacks);

    }


}
