/*
 @todo properly import editor css without link tag in layout html
 */
import {Editor} from "@toast-ui/editor";
const content = "derp"
const editor = new Editor({
    el: document.querySelector('#editor'),
    previewStyle: 'vertical',
    height: '500px',
    initialValue: content,
    usageStatistics: false,
    initialEditType: 'wysiwyg'
});

let createArticleForm = document.getElementById("createArticleForm");
if (createArticleForm){
    createArticleForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const hiddenBodyInput = document.getElementById("hiddenBody")
        hiddenBodyInput.value = editor.getMarkdown()
        createArticleForm.submit()
    });
}

