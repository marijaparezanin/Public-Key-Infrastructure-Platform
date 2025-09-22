<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "user-profile-commons.ftl" as userProfileCommons>
<#import "register-commons.ftl" as registerCommons>
<#import "password-validation.ftl" as validator>

<@layout.registrationLayout displayMessage=messagesPerField.exists('global') displayRequiredFields=true; section>

<#if section == "header">
    <#if messageHeader??>
        ${kcSanitize(msg("${messageHeader}"))?no_esc}
    <#else>
        ${msg("registerTitle")}
    </#if>

<#elseif section == "form">
    <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post" novalidate="novalidate">
        <@userProfileCommons.userProfileFormFields; callback, attribute>
            <#if callback == "afterField">
                <#if passwordRequired?? && (attribute.name == 'username' || (attribute.name == 'email' && realm.registrationEmailAsUsername))>
                    <@field.password name="password" fieldName="password" required=true label=msg("password") autocomplete="new-password"/>
                    <div id="password-strength-container" style="margin-top:5px;">
                        <div id="password-strength-bar" style="height:8px; width:100%; background:#ddd; border-radius:4px; overflow:hidden;">
                            <div id="password-strength-fill" style="height:100%; width:0%; background:red; transition: width 0.3s;"></div>
                        </div>
                        <div id="password-strength-text" style="font-weight:bold; margin-top:3px;">Strength: </div>
                        <div id="password-disabled-message" style="color:red; font-size:0.9em; margin-top:3px; display:none;">
                            You cannot register: password is too weak.
                        </div>
                    </div>
                    <@field.password name="password-confirm" fieldName="password-confirm" required=true label=msg("passwordConfirm") autocomplete="new-password"/>
                </#if>
            </#if>
        </@userProfileCommons.userProfileFormFields>

        <@registerCommons.termsAcceptance/>

        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
            <input id="kc-submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doRegister")}"/>
        </div>

        <div class="${properties.kcFormGroupClass!} pf-v5-c-login__main-footer-band">
            <div id="kc-form-options" class="${properties.kcFormOptionsClass!} pf-v5-c-login__main-footer-band-item">
                <div class="${properties.kcFormOptionsWrapperClass!}">
                    <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                </div>
            </div>
        </div>
    </form>

    <@validator.templates/>
    <@validator.script field="password"/>

    <!-- Classic zxcvbn library -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/zxcvbn/4.4.2/zxcvbn.js"></script>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const passwordField = document.querySelector('input[name="password"]');
            const strengthFill = document.getElementById('password-strength-fill');
            const strengthText = document.getElementById('password-strength-text');
            const disabledMessage = document.getElementById('password-disabled-message');
            const submitBtn = document.getElementById('kc-submit');

            const colors = ["#ff4d4f", "#ff7a45", "#ffa940", "#bae637", "#52c41a"];
            const labels = ["Very Weak","Weak","Fair","Good","Strong"];

            // Initially disable submit button
            submitBtn.disabled = true;

            passwordField.addEventListener('input', function() {
                const val = passwordField.value;
                const result = zxcvbn(val);
                const score = result.score; // 0-4
                const feedback = result.feedback.suggestions.join(' ');

                // Update bar
                const percentage = ((score + 1) / 5) * 100;
                strengthFill.style.width = percentage + "%";
                strengthFill.style.backgroundColor = colors[score];

                // Update text
                strengthText.textContent = "Strength: " + labels[score];
                if(feedback) {
                    strengthText.textContent += " - " + feedback;
                }

                // Enable submit only if score >= 2 ("Fair")
                if(score >= 2){
                    submitBtn.disabled = false;
                    disabledMessage.style.display = "none";
                } else {
                    submitBtn.disabled = true;
                    disabledMessage.style.display = "block";
                }
            });
        });
    </script>

</#if>

</@layout.registrationLayout>
