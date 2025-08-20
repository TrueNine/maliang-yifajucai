import type {AttachmentTyping} from '../enums/';

/**
 * 附件 以及 附件链接
 */
export interface Dynamic_Attachment {
  id?: string;
  crd?: string | undefined;
  mrd?: string | undefined;
  rlv?: number;
  ldf?: string | undefined;
  saveName?: string | undefined;
  metaName?: string | undefined;
  attType?: AttachmentTyping;
  /**
   * 该链接下的所有子文件
   */
  childAttachments?: Array<Dynamic_Attachment>;
  baseUrl?: string | undefined;
  baseUri?: string | undefined;
  urlName?: string | undefined;
  urlDoc?: string | undefined;
  urlId?: number | undefined;
  parentUrlAttachment?: Dynamic_Attachment | undefined;
  size?: number | undefined;
  mimeType?: string | undefined;
  fullAccessUrl?: string | undefined;
}
