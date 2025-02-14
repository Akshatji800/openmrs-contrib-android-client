/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.test.presenters;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.openmrs.android_sdk.library.OpenMRSLogger;
import com.openmrs.android_sdk.library.OpenmrsAndroid;
import com.openmrs.android_sdk.library.dao.ProviderRoomDAO;
import com.openmrs.android_sdk.library.models.Provider;
import com.openmrs.android_sdk.utilities.NetworkUtils;
import com.openmrs.android_sdk.utilities.ToastUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.mobile.activities.providermanagerdashboard.ProviderManagerDashboardContract;
import org.openmrs.mobile.activities.providermanagerdashboard.ProviderManagerDashboardPresenter;
import com.openmrs.android_sdk.library.api.RestApi;
import com.openmrs.android_sdk.library.api.repository.ProviderRepository;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.test.ACUnitTestBase;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({NetworkUtils.class, ToastUtil.class, OpenMRS.class, OpenMRSLogger.class,OpenmrsAndroid.class})
public class ProviderManagerDashboardPresenterTest extends ACUnitTestBase {
    @Rule
    public InstantTaskExecutorRule taskExecutorRule = new InstantTaskExecutorRule();
    @Mock
    private RestApi restApi;
    @Mock
    private ProviderManagerDashboardContract.View providerManagerView;
    @Mock
    private Observer<List<Provider>> observer;
    @Mock
    private OpenMRSLogger openMRSLogger;
    @Mock
    private OpenMRS openMRS;
    MutableLiveData<List<Provider>> providerLiveData = Mockito.mock(MutableLiveData.class);
    private ProviderManagerDashboardPresenter providerManagerDashboardPresenter;
    private ProviderRepository providerRepository;
    private Fragment fragment = new Fragment();
    List<Provider> providerList;
    Provider providerOne = createProvider(1l, "doctor");
    Provider providerTwo = createProvider(2l, "nurse");

    @Before
    public void setUp() {
        providerList = Arrays.asList(providerOne, providerTwo);
        providerLiveData.postValue(providerList);

        this.providerRepository = new ProviderRepository(restApi, openMRSLogger);
        ProviderRoomDAO providerRoomDao = Mockito.mock(ProviderRoomDAO.class, RETURNS_MOCKS);
        ProviderRoomDAO spyProviderRoomDao = spy(providerRoomDao);

        Single listSingle = Mockito.mock(Single.class);
        doNothing().when(spyProviderRoomDao).updateProviderByUuid(Mockito.anyString(), Mockito.anyLong(), Mockito.any(), Mockito.anyString(), Mockito.anyString());
        when(spyProviderRoomDao.getProviderList()).thenReturn(listSingle);
        when(listSingle.blockingGet()).thenReturn(providerList);

        this.providerRepository.setProviderRoomDao(spyProviderRoomDao);

        this.providerManagerDashboardPresenter = new ProviderManagerDashboardPresenter(providerManagerView, restApi, providerRepository);
        mockStaticMethods();
    }

    @Test
    public void shouldGetProviders_AllOK() {

        Provider providerOne = createProvider(1l, "doctor");
        Provider providerTwo = createProvider(2l, "nurse");
        providerList = Arrays.asList(providerOne, providerTwo);
        providerLiveData.postValue(providerList);

        when(NetworkUtils.isOnline()).thenReturn(true);
        when(restApi.getProviderList()).thenReturn(mockSuccessCall(providerList));

        providerManagerDashboardPresenter.updateViews(providerList);
        providerRepository.getProviders().observeForever(observer);

        verify(restApi).getProviderList();
        verify(providerManagerView).updateAdapter(providerList);
        verify(providerManagerView).updateVisibility(true, null);
    }

    @Test
    public void shouldGetProviders_Error() {
        Mockito.lenient().when(NetworkUtils.isOnline()).thenReturn(true);
        Mockito.lenient().when(restApi.getProviderList()).thenReturn(mockErrorCall(401));

        providerManagerDashboardPresenter.getProviders(fragment);
        verify(restApi).getProviderList();
    }

    @Test
    public void shouldGetProviders_EmptyList() {
        List<Provider> providerList = new ArrayList<>();
        providerLiveData.postValue(providerList);

        Mockito.lenient().when(NetworkUtils.isOnline()).thenReturn(true);
        when(restApi.getProviderList()).thenReturn(mockSuccessCall(providerList));
        providerManagerDashboardPresenter.getProviders(fragment);
        providerManagerDashboardPresenter.updateViews(providerList);
        verify(restApi).getProviderList();
        verify(providerManagerView).updateVisibility(false, "No Data to display.");
    }

    private void mockStaticMethods() {
        PowerMockito.mockStatic(NetworkUtils.class);
        PowerMockito.mockStatic(OpenMRS.class);
        PowerMockito.mockStatic(OpenMRSLogger.class);
        PowerMockito.mockStatic(OpenmrsAndroid.class);
        Mockito.lenient().when(OpenMRS.getInstance()).thenReturn(openMRS);
        PowerMockito.when(OpenmrsAndroid.getOpenMRSLogger()).thenReturn(openMRSLogger);
        PowerMockito.mockStatic(ToastUtil.class);
    }

    public abstract class Single extends io.reactivex.Single {
    }
}
