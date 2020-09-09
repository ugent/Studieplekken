import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';

@Component({
  selector: 'app-location-description',
  templateUrl: './location-description.component.html',
  styleUrls: ['./location-description.component.css']
})
export class LocationDescriptionComponent implements OnInit {
  @Input() location: Observable<Location>;

  editor = ClassicEditor;

  model = {
    description: '<p>Hello, world!</p>'
  };

  config = {
    toolbar: [
      'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo',
      'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt',
      'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField',
      '/',
      'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'CopyFormatting', 'RemoveFormat',
      'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language',
      'Link', 'Unlink', 'Anchor',
      'Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'Iframe',
      '/',
      'Styles', 'Format', 'Font', 'FontSize',
      'TextColor', 'BGColor',
      'Maximize', 'ShowBlocks',
      'About'
    ]
  };

  constructor() { }

  ngOnInit(): void {
    this.editor.editorConfig
  }

  buttonClick(): void {
    console.log(this.model.description);
  }
}
