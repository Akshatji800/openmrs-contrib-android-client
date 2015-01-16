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

package org.openmrs.mobile.net.volley.wrappers;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.openmrs.mobile.application.OpenMRS;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiPartRequest extends Request<String> {

    private MultipartEntity entity = new MultipartEntity();

    private static final String FILE_PART_NAME = "xml_submission_file";

    private final Response.Listener<String> mListener;
    private final File mFilePart;
    private final String mPatientUUID;

    public MultiPartRequest(String url, Response.ErrorListener errorListener, Response.Listener<String> listener, File file, String patientUUID) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mFilePart = file;
        mPatientUUID = patientUUID;
        buildMultiPartEntity();
    }

    private void buildMultiPartEntity() {
        entity.addPart(FILE_PART_NAME, new FileBody(mFilePart));
    }

    @Override
    public String getBodyContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success("Uploaded", getCacheEntry());
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();

        StringBuilder builder = new StringBuilder();
        builder.append(ApplicationConstants.JSESSIONID_PARAM);
        builder.append("=");
        builder.append(OpenMRS.getInstance().getSessionToken());
        params.put(ApplicationConstants.COOKIE_PARAM, builder.toString());
        params.put(ApplicationConstants.PATIENT_UUID_PARAM, mPatientUUID);
        return params;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
