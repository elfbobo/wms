package com.cxy.wms.controller;
import com.cxy.wms.entity.WmsWareHouseEntity;
import com.cxy.wms.service.WmsWareHouseServiceI;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.jeecgframework.core.util.MyBeanUtils;

import java.io.OutputStream;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.jeecgframework.core.util.ResourceUtil;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import org.jeecgframework.core.util.ExceptionUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.jeecgframework.core.beanvalidator.BeanValidators;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.jwt.util.ResponseMessage;
import org.jeecgframework.jwt.util.Result;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**   
 * @Title: Controller  
 * @Description: 仓库信息表
 * @author onlineGenerator
 * @date 2018-10-17 19:54:50
 * @version V1.0   
 *
 */
@Api(value="WmsWareHouse",description="仓库信息表",tags="wmsWareHouseController")
@Controller
@RequestMapping("/wmsWareHouseController")
public class WmsWareHouseController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(WmsWareHouseController.class);

	@Autowired
	private WmsWareHouseServiceI wmsWareHouseService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private Validator validator;
	


	/**
	 * 仓库信息表列表 页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("com/cxy/wms/wmsWareHouseList");
	}

	/**
	 * easyui AJAX请求数据
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(WmsWareHouseEntity wmsWareHouse,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(WmsWareHouseEntity.class, dataGrid);
		//查询条件组装器
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, wmsWareHouse, request.getParameterMap());
		try{
			//自定义追加查询条件
			cq.eq("isdel", 0);
		}catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		cq.add();
		this.wmsWareHouseService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}
	
	/**
	 * 删除仓库信息表
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(WmsWareHouseEntity wmsWareHouse, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		wmsWareHouse = systemService.getEntity(WmsWareHouseEntity.class, wmsWareHouse.getId());
		message = "仓库信息表删除成功";
		if(wmsWareHouse.getHouseStatus() != 3){
			message = "删除失败，只有已经废弃的仓库才能删除";
		}else{
			try{
				WmsWareHouseEntity t = wmsWareHouseService.get(WmsWareHouseEntity.class, wmsWareHouse.getId());
				wmsWareHouse.setIsdel(1);
				MyBeanUtils.copyBeanNotNull2Bean(wmsWareHouse, t);
				wmsWareHouseService.saveOrUpdate(t);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}catch(Exception e){
				e.printStackTrace();
				message = "仓库信息表删除失败";
				throw new BusinessException(e.getMessage());
			}
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 批量删除仓库信息表
	 * 
	 * @return
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "仓库信息表删除成功";
		try{
			for(String id:ids.split(",")){
				WmsWareHouseEntity wmsWareHouse = systemService.getEntity(WmsWareHouseEntity.class, 
				id
				);
				wmsWareHouseService.delete(wmsWareHouse);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "仓库信息表删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加仓库信息表
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(WmsWareHouseEntity wmsWareHouse, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "仓库信息表添加成功";
		try{
			wmsWareHouse.setIsdel(0);
			wmsWareHouseService.save(wmsWareHouse);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "仓库信息表添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 更新仓库信息表
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(WmsWareHouseEntity wmsWareHouse, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "仓库信息表更新成功";
		WmsWareHouseEntity t = wmsWareHouseService.get(WmsWareHouseEntity.class, wmsWareHouse.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(wmsWareHouse, t);
			wmsWareHouseService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "仓库信息表更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	

	/**
	 * 仓库信息表新增页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(WmsWareHouseEntity wmsWareHouse, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(wmsWareHouse.getId())) {
			wmsWareHouse = wmsWareHouseService.getEntity(WmsWareHouseEntity.class, wmsWareHouse.getId());
			req.setAttribute("wmsWareHouse", wmsWareHouse);
		}
		return new ModelAndView("com/cxy/wms/wmsWareHouse-add");
	}
	/**
	 * 仓库信息表编辑页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(WmsWareHouseEntity wmsWareHouse, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(wmsWareHouse.getId())) {
			wmsWareHouse = wmsWareHouseService.getEntity(WmsWareHouseEntity.class, wmsWareHouse.getId());
			req.setAttribute("wmsWareHouse", wmsWareHouse);
		}
		return new ModelAndView("com/cxy/wms/wmsWareHouse-update");
	}
	
	/**
	 * 导入功能跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		req.setAttribute("controller_name","wmsWareHouseController");
		return new ModelAndView("common/upload/pub_excel_upload");
	}
	
	/**
	 * 导出excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(WmsWareHouseEntity wmsWareHouse,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
		CriteriaQuery cq = new CriteriaQuery(WmsWareHouseEntity.class, dataGrid);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, wmsWareHouse, request.getParameterMap());
		List<WmsWareHouseEntity> wmsWareHouses = this.wmsWareHouseService.getListByCriteriaQuery(cq,false);
		modelMap.put(NormalExcelConstants.FILE_NAME,"仓库信息表");
		modelMap.put(NormalExcelConstants.CLASS,WmsWareHouseEntity.class);
		modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("仓库信息表列表", "导出人:"+ResourceUtil.getSessionUser().getRealName(),
			"导出信息"));
		modelMap.put(NormalExcelConstants.DATA_LIST,wmsWareHouses);
		return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	/**
	 * 导出excel 使模板
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public String exportXlsByT(WmsWareHouseEntity wmsWareHouse,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
    	modelMap.put(NormalExcelConstants.FILE_NAME,"仓库信息表");
    	modelMap.put(NormalExcelConstants.CLASS,WmsWareHouseEntity.class);
    	modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("仓库信息表列表", "导出人:"+ResourceUtil.getSessionUser().getRealName(),
    	"导出信息"));
    	modelMap.put(NormalExcelConstants.DATA_LIST,new ArrayList());
    	return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "importExcel", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson importExcel(HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();// 获取上传文件对象
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<WmsWareHouseEntity> listWmsWareHouseEntitys = ExcelImportUtil.importExcel(file.getInputStream(),WmsWareHouseEntity.class,params);
				for (WmsWareHouseEntity wmsWareHouse : listWmsWareHouseEntitys) {
					wmsWareHouseService.save(wmsWareHouse);
				}
				j.setMsg("文件导入成功！");
			} catch (Exception e) {
				j.setMsg("文件导入失败！");
				logger.error(ExceptionUtil.getExceptionMessage(e));
			}finally{
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return j;
	}
	
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="仓库信息表列表信息",produces="application/json",httpMethod="GET")
	public ResponseMessage<List<WmsWareHouseEntity>> list() {
		List<WmsWareHouseEntity> listWmsWareHouses=wmsWareHouseService.getList(WmsWareHouseEntity.class);
		return Result.success(listWmsWareHouses);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="根据ID获取仓库信息表信息",notes="根据ID获取仓库信息表信息",httpMethod="GET",produces="application/json")
	public ResponseMessage<?> get(@ApiParam(required=true,name="id",value="ID")@PathVariable("id") String id) {
		WmsWareHouseEntity task = wmsWareHouseService.get(WmsWareHouseEntity.class, id);
		if (task == null) {
			return Result.error("根据ID获取仓库信息表信息为空");
		}
		return Result.success(task);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="创建仓库信息表")
	public ResponseMessage<?> create(@ApiParam(name="仓库信息表对象")@RequestBody WmsWareHouseEntity wmsWareHouse, UriComponentsBuilder uriBuilder) {
		//调用JSR303 Bean Validator进行校验，如果出错返回含400错误码及json格式的错误信息.
		Set<ConstraintViolation<WmsWareHouseEntity>> failures = validator.validate(wmsWareHouse);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//保存
		try{
			wmsWareHouseService.save(wmsWareHouse);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("仓库信息表信息保存失败");
		}
		return Result.success(wmsWareHouse);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="更新仓库信息表",notes="更新仓库信息表")
	public ResponseMessage<?> update(@ApiParam(name="仓库信息表对象")@RequestBody WmsWareHouseEntity wmsWareHouse) {
		//调用JSR303 Bean Validator进行校验，如果出错返回含400错误码及json格式的错误信息.
		Set<ConstraintViolation<WmsWareHouseEntity>> failures = validator.validate(wmsWareHouse);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//保存
		try{
			wmsWareHouseService.saveOrUpdate(wmsWareHouse);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("更新仓库信息表信息失败");
		}

		//按Restful约定，返回204状态码, 无内容. 也可以返回200状态码.
		return Result.success("更新仓库信息表信息成功");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value="删除仓库信息表")
	public ResponseMessage<?> delete(@ApiParam(name="id",value="ID",required=true)@PathVariable("id") String id) {
		logger.info("delete[{}]" , id);
		// 验证
		if (StringUtils.isEmpty(id)) {
			return Result.error("ID不能为空");
		}
		try {
			wmsWareHouseService.deleteEntityById(WmsWareHouseEntity.class, id);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("仓库信息表删除失败");
		}

		return Result.success();
	}
}
