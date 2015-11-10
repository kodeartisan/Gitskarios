package com.alorma.github.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Pair;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.request.RepoRequestDTO;
import com.alorma.github.sdk.bean.dto.response.GitIgnoreTemplates;
import com.alorma.github.sdk.services.gtignore.GitIgnoreClient;
import com.alorma.github.ui.activity.ContentEditorActivity;
import java.util.ArrayList;
import retrofit.client.Response;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by bernat.borras on 10/11/15.
 */
public class CreateRepositoryFragment extends PreferenceFragment {

  private static final int DESCRIPTION_EDIT = 112;

  private RepoRequestDTO repoRequestDTO = new RepoRequestDTO();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.repository_create_prefs);

    final EditTextPreference pref_repo_name = (EditTextPreference) findPreference("pref_repo_name");
    pref_repo_name.setTitle(repoRequestDTO.name);
    pref_repo_name.setText(repoRequestDTO.name);
    pref_repo_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        repoRequestDTO.name = (String) newValue;
        pref_repo_name.setTitle(repoRequestDTO.name);
        return true;
      }
    });

    final Preference pref_repo_description = findPreference("pref_repo_description");
    pref_repo_description.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        Intent intent = ContentEditorActivity.createLauncherIntent(getActivity(), null,
            repoRequestDTO.description, true, true);
        startActivityForResult(intent, DESCRIPTION_EDIT);
        return false;
      }
    });

    final Preference pref_repo_url = findPreference("pref_repo_url");
    pref_repo_url.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        repoRequestDTO.homepage = (String) newValue;
        return false;
      }
    });

    CheckBoxPreference pref_repo_has_issues =
        (CheckBoxPreference) findPreference("pref_repo_has_issues");
    pref_repo_has_issues.setChecked(repoRequestDTO.has_issues);
    pref_repo_has_issues.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        repoRequestDTO.has_issues = (Boolean) newValue;
        return true;
      }
    });

    CheckBoxPreference pref_repo_has_wiki =
        (CheckBoxPreference) findPreference("pref_repo_has_wiki");
    pref_repo_has_wiki.setChecked(repoRequestDTO.has_wiki);
    pref_repo_has_wiki.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        repoRequestDTO.has_wiki = (Boolean) newValue;
        return true;
      }
    });

    CheckBoxPreference pref_repo_has_downloads =
        (CheckBoxPreference) findPreference("pref_repo_has_downloads");
    pref_repo_has_downloads.setChecked(repoRequestDTO.has_downloads);
    pref_repo_has_downloads.setOnPreferenceChangeListener(
        new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            repoRequestDTO.has_downloads = (Boolean) newValue;
            return true;
          }
        });

    final MaterialListPreference pref_repo_gitignore =
        (MaterialListPreference) findPreference("pref_repo_gitignore");
    pref_repo_gitignore.setEnabled(false);
    pref_repo_gitignore.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!newValue.equals("[none]")) {
          repoRequestDTO.gitignore_template = (String) newValue;
          pref_repo_gitignore.setSummary((String) newValue);
        } else {
          repoRequestDTO.gitignore_template = null;
          pref_repo_gitignore.setSummary("");
        }
        return false;
      }
    });

    GitIgnoreClient gitIgnoreClient = new GitIgnoreClient(getActivity());
    gitIgnoreClient.observable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Pair<GitIgnoreTemplates, Response>>() {
          @Override
          public void onCompleted() {
          }

          @Override
          public void onError(Throwable e) {

          }

          @Override
          public void onNext(Pair<GitIgnoreTemplates, Response> gitIgnoreTemplatesResponsePair) {
            ArrayList<String> ignoreTemplates = new ArrayList<String>();
            ignoreTemplates.add("[none]");
            ignoreTemplates.addAll(gitIgnoreTemplatesResponsePair.first);
            CharSequence[] templates = new CharSequence[ignoreTemplates.size()];
            for (int i = 0; i < ignoreTemplates.size(); i++) {
              templates[i] = ignoreTemplates.get(i);
            }
            pref_repo_gitignore.setEntries(templates);
            pref_repo_gitignore.setEntryValues(templates);
            pref_repo_gitignore.setEnabled(true);
          }
        });
  }
}
