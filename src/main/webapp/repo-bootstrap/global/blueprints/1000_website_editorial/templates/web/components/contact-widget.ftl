<#import "/templates/system/common/crafter.ftl" as crafter />

<@studio.componentRootTag $tag="section">
  <header class="major">
    <@studio.h2 $field="title_t">${contentModel.title_t}</@studio.h2>
  </header>
  <@studio.div $field="text_html">
    ${contentModel.text_html}
  </@studio.div>
  <ul class="contact">
    <li class="icon solid fa-envelope">
      <@studio.a href="mailto:${contentModel.email_s}" $field="email_s">${contentModel.email_s}</@studio.a>
    </li>
    <@studio.li class="icon solid fa-phone" $field="phone_s">
      ${contentModel.phone_s}
    </@studio.li>
    <@studio.li class="icon solid fa-home" $field="address_html">
      ${contentModel.address_html}
    </@studio.li>
  </ul>
</@studio.componentRootTag>
