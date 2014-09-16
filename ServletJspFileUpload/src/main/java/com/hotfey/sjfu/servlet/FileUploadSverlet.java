package com.hotfey.sjfu.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileCleaningTracker;

/**
 * Servlet implementation class FileUploadSverlet
 */
public class FileUploadSverlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private File fileUploadPath;
	private String realPath;
	private String fileDirectroy;

	@Override
	public void init() {
		realPath = this.getServletConfig().getServletContext().getRealPath("/");
		fileDirectroy = this.getServletConfig().getInitParameter(
				"fileDirectroy");
		fileUploadPath = new File(realPath + fileDirectroy);
		if (!fileUploadPath.exists()) {
			fileUploadPath.mkdirs();
		}
	}

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileUploadSverlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String filePath = request.getParameter("filePath");
		if (filePath != null && !filePath.isEmpty()) {
			PrintWriter printWriter = response.getWriter();
			response.setContentType("application/json");
			JSONArray files = new JSONArray();
			try {
				File removeFile = new File(fileUploadPath, filePath);
				if (removeFile.exists()) {
					boolean flag = removeFile.delete();
					JSONObject file = new JSONObject();
					if (flag) {
						file.accumulate("deleteWithCredentials", flag);
					}
					files.add(file);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JSONObject result = new JSONObject();
				result.accumulate("files", files);
				printWriter.write(result.toString());
				if (printWriter != null) {
					printWriter.close();
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {
			// Configure a repository (to ensure a secure temp location is used)
			ServletContext servletContext = this.getServletConfig()
					.getServletContext();
			File repository = (File) servletContext
					.getAttribute("javax.servlet.context.tempdir");
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = newDiskFileItemFactory(
					servletContext, repository);

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			PrintWriter printWriter = response.getWriter();
			response.setContentType("application/json");
			JSONArray files = new JSONArray();
			try {
				// Parse the request
				List<FileItem> items = upload.parseRequest(request);
				// Process the uploaded items
				Iterator<FileItem> iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = iter.next();
					if (item.isFormField()) {
						System.out.println(item.getFieldName());
					} else {
						String name = item.getName();
						if (!name.equals("")) {
							int index = name.indexOf("\\");
							File uploadedFile = null;
							if (index == -1) {
								uploadedFile = new File(fileUploadPath,
										File.separator + name);
							} else {
								uploadedFile = new File(
										fileUploadPath,
										File.separator
												+ name.substring(name
														.lastIndexOf(File.separator) + 1));
							}
							item.write(uploadedFile);
							JSONObject file = new JSONObject();
							file.accumulate("name", name);
							file.accumulate("size", item.getSize());
							file.accumulate("url", ".." + fileDirectroy + "/"
									+ name);
							file.accumulate("deleteUrl",
									"../FileUploadSverlet?filePath=" + name);
							// it's better to use POST,
							// because when use GET, IE will get from cache first
							file.accumulate("deleteType", "post");
							files.add(file);
						}
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JSONObject result = new JSONObject();
				result.accumulate("files", files);
				printWriter.write(result.toString());
				if (printWriter != null) {
					printWriter.close();
				}
			}
		} else {
			doGet(request, response);
		}
	}

	// Resource cleanup
	public DiskFileItemFactory newDiskFileItemFactory(ServletContext context,
			File repository) {
		FileCleaningTracker fileCleaningTracker = FileCleanerCleanup
				.getFileCleaningTracker(context);
		DiskFileItemFactory factory = new DiskFileItemFactory(
				DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository);
		factory.setFileCleaningTracker(fileCleaningTracker);
		return factory;
	}

}
