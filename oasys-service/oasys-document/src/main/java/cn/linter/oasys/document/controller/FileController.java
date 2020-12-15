package cn.linter.oasys.document.controller;

import cn.linter.oasys.common.entity.Response;
import cn.linter.oasys.document.entity.File;
import cn.linter.oasys.document.service.FileService;
import com.github.pagehelper.PageInfo;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 文件控制器
 *
 * @author wangxiaoyang
 * @since 2020/11/10
 */
@Api(tags = "文件控制器")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @ApiOperation("通过文件实例分页查询所有文件")
    @GetMapping("files")
    public Response<List<File>> listFile(@RequestParam(defaultValue = "1") @ApiParam("页号") int pageNumber,
                                         @RequestParam(defaultValue = "10") @ApiParam("页大小") int pageSize,
                                         @ApiParam("文件实例") File file) {
        PageInfo<File> pageInfo = fileService.list(pageNumber, pageSize, file);
        return Response.sendSuccess(200, pageInfo.getList(), pageInfo.getTotal());
    }

    @ApiOperation("创建文件夹")
    @PostMapping("folder")
    public Response<File> createFolder(@RequestBody @ApiParam("文件夹实例") File file) {
        return Response.sendSuccess(201, fileService.createFolder(file));
    }

    @ApiOperation("上传文件")
    @PostMapping("file")
    public Response<File> uploadFile(@ApiParam("二进制文件") MultipartFile multipartFile, @ApiParam("文件实例") File file) throws Exception {
        return Response.sendSuccess(201, fileService.uploadFile(multipartFile, file));
    }

    @ApiOperation("下载文件")
    @GetMapping("file/{id}")
    public void downloadFile(@PathVariable("id") @ApiParam("文件ID") Long id, HttpServletResponse response) throws ServerException, InsufficientDataException,
            NoSuchAlgorithmException, InternalException, InvalidResponseException, XmlParserException, InvalidKeyException, ErrorResponseException {
        try (InputStream in = fileService.downloadFile(id, response); OutputStream out = response.getOutputStream()) {
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation("更新文件")
    @PutMapping("file")
    public Response<File> updateFile(@RequestBody @ApiParam("文件实例") File file) {
        File updatedFile = fileService.update(file);
        if (updatedFile != null) {
            return Response.sendSuccess(200, updatedFile);
        }
        return Response.sendError(404, "文件不存在！");
    }

    @ApiOperation("删除文件")
    @DeleteMapping("file/{id}")
    public Response<?> deleteFile(@PathVariable("id") @ApiParam("文件ID") Long id) throws IOException, InvalidResponseException, InvalidKeyException,
            NoSuchAlgorithmException, ServerException, ErrorResponseException, XmlParserException, InsufficientDataException, InternalException {
        if (fileService.delete(id)) {
            return Response.sendSuccess(200);
        }
        return Response.sendError(404, "文件不存在！");
    }

}